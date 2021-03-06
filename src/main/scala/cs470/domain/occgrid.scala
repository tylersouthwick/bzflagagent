package cs470.domain

import collection.mutable.{HashMap, LinkedList}
import cs470.domain._
import Constants._
import cs470.utils.{Degree, DefaultProperties, Properties}
import cs470.bzrc.{Enemy, Tank, RefreshableEnemies, DataStore}

/**
 * @author tylers2
 */

trait Occgrid {
	implicit def double2int(d: Double) = d.toInt

	def data(x: Int)(y: Int): Occupant.Occupant

	def offset: (Int, Int)

	def width: Int

	def height: Int

	def convert(location: Point): (Int, Int)

	def getLocation(x: Int, y: Int): Point

	def print: String

	import java.lang.Math._
	def hasLineOfSight(start : Point, end : Point) : Boolean = {
		val (s, e) = (convert(start), convert(end))
		val slope = (e._2 - s._2).asInstanceOf[Double]/(e._1 - s._1).asInstanceOf[Double]
		val intercept = s._2 - slope * s._1
		for (x <- (min(s._1, e._1) to max(s._1, e._1))) {
			if (data(x)(floor(slope * x + intercept).asInstanceOf[Int]) == Occupant.WALL) {
				return false
			}
		}
		true
	}
}

trait UpdateableOccgrid extends ((Int, Int) => Double) with Occgrid {
	def size: Int

	def lock: Object
}

trait BayesianOccgrid extends Occgrid with UpdateableOccgrid {
	val LOG = org.apache.log4j.Logger.getLogger("cs470.domain.BayesianOccgrid")
	private val cutoff: Double = Properties("bayesianCutoff", 0.95)


	def getClosestUnexplored(point: Point, within: Double): Point = {
		import scala.math.{cos, sin}
		val angles = Seq.range(0, 360, 10)
		println("looking for closest unexplored point: " + (point, within))
		angles.foreach {
			angle =>
				val a = Degree(angle).radian
				val p = point + new Point(within * cos(a), within * sin(a))
				val np = convert(p)
				if (P_s(np._1, np._2) == DefaultProperties.prior && data(np._1)(np._2) != Occupant.WALL) {
					return p
				}
		}
		null
	}

	def offset: (Int, Int) = (size / 2, size / 2 - 1)

	def height = size

	def width = size

	def P_s(x: Int, y: Int, d : Double) {
		try {
			myData(x)(y) = d
		} catch {
			case _ =>
		}
	}

	def P_s(x: Int, y: Int) = try {
		myData(x)(y)
	} catch {
		case _ => 1.0
	}

	def P_ns(x: Int, y: Int) = 1 - P_s(x, y)

	def P_o_s = TP

	def P_no_s = 1 - P_o_s

	def P_no_ns = TN

	def P_o_ns = 1 - P_no_ns

	def P_o(x: Int, y: Int) = P_o_s * P_s(x, y) + P_o_ns * (1 - P_s(x, y))

	def P_no(x: Int, y: Int) = 1 - P_o(x, y)

	def P_s_o(x: Int, y: Int) = (P_o_s * P_s(x, y)) / P_o(x, y)

	def P_s_no(x: Int, y: Int) = (P_no_s * P_s(x, y)) / P_no(x, y)

	def apply(x: Int, y: Int) = myData(x)(y)

	val constants: Constants
	lazy val size: Int = constants("worldsize")
	private lazy val TP = {
		val t: Double = constants("truepositive")
		LOG.debug("True Positive Rate = " + t)
		t
	}
	private lazy val TN = {
		val t: Double = constants("truenegative")
		LOG.debug("True Negative Rate = " + t)
		t
	}

	private lazy val myData: Array[Array[Double]] = {
		val data = Array.ofDim[Double](size, size)
		for (x <- 0 until data.length) {
			for (y <- 0 until data.length) {
				data(x)(y) = DefaultProperties.prior
			}
		}
		data
	}

	val lock = new Object

	def update(occgrids : Traversable[OccgridCommand]) {
		lock synchronized {
			occgrids.foreach(doUpdate)
		}
	}

	private def doUpdate(grid: Occgrid) {
		for (gx <- 0 until grid.width - 1) {
			for (gy <- 0 until grid.height - 1) {
				val (x, y) = convert(grid.getLocation(gx, gy))
				myData(x)(y) = grid.data(gx)(gy) match {
					case Occupant.NONE => P_s_no(x, y)
					case Occupant.WALL => P_s_o(x, y)
				}
			}
		}

		LOG.debug("Done updating")
	}

	def print: String = ""

	def convert(location: Point) = (location.x.intValue + offset._1, offset._2 - location.y.intValue)

	def getLocation(x: Int, y: Int) = {
		new Point(x - offset._1, offset._2 - y)
	}

	def data(x: Int)(y: Int) = {
		try {
			if (myData(x)(y) > cutoff) {
				Occupant.WALL
			} else {
				Occupant.NONE
			}
		} catch {
			case _ => Occupant.WALL
		}
	}

}

class UsableOccgrid(resolution: Int, obstacles: Seq[Polygon], tankRadius: Double, val worldSize: Int, enemies: Seq[Enemy]) extends Occgrid with Traversable[Array[Occupant.Occupant]] {
	val alpha = math.floor(worldSize / resolution)
	val width = resolution
	val height = resolution
	val offset: (Int, Int) = (resolution / 2, resolution / 2)

	private val myData: Array[Array[Occupant.Occupant]] = Array.ofDim(width, height);

	def data(x: Int)(y: Int) = myData(y)(x)

	fillArray

	def inCircle(xs: Double, ys: Double, point: Point, radius: Double) = {
		val radii_squared = (alpha / 2 + radius)
		val distance_squared = (new Point(xs, ys)).distance(point)

		if (radii_squared > distance_squared) {
			true
		} else {
			false
		}
	}

	def inPolygon(x: Double, y: Double, poly: Polygon): Boolean = {
		val bx1 = x;
		val by1 = y;
		val bx2 = x + alpha;
		val by2 = y - alpha;

		val ax1 = poly.topLeft.x
		val ay1 = poly.topLeft.y
		val ax2 = poly.bottomRight.x
		val ay2 = poly.bottomRight.y

		if (ax1 <= bx2 && ax2 >= bx1 && ay1 >= by2 && ay2 <= by1) {
			true
		} else {
			false
		}
	}

	def checkObstacle(x: Double, y: Double): Boolean = {
		obstacles.foreach {
			obstacle =>
				if (inPolygon(x, y, obstacle)) return true
		}

		false
	}

	def checkEnemy(x: Double, y: Double): Boolean = {
		enemies.foreach {
			enemy =>
				if (inCircle(x, y, enemy.location, tankRadius)) return true
		}

		false
	}

	def foreach[U](f: (Array[Occupant.Occupant]) => U) {
		myData.foreach(f)
	}

	def convert(location: Point) = {
		val t: (Int, Int) = (location.x.intValue / alpha + offset._1, offset._2 + location.y.intValue / alpha)
		//    Console.out.println(t)
		t
	}

	override def getLocation(x: Int, y: Int) = {
		new Point((x - offset._1) * alpha, (y - offset._2) * alpha)
	}

	def fillArray = {

		(0 to width - 1).foreach {
			x =>
				(0 to height - 1).foreach {
					y =>
						val tmp = getLocation(x, y)
						//            if (checkEnemy(tmp.x, tmp.y)) {
						//myData(y)(x) = Occupant.ENEMY
						//              myData(y)(x) = Occupant.NONE
						//            } else
						if (checkObstacle(tmp.x, tmp.y)) {
							myData(y)(x) = Occupant.WALL
						} else {
							myData(y)(x) = Occupant.NONE
						}

				}
		}

		myData
	}

	def print: String = {
		val sb = new StringBuilder

		myData.foreach {
			rows =>
				rows.foreach {
					case Occupant.ENEMY => sb.append("\tE")
					case Occupant.WALL => sb.append("\tW")
					case Occupant.NONE => sb.append("\t ")
					case _ => sb.append("\t!")
				}
				sb.append("\n")
		}

		sb.toString()
	}

	override def toString() = {
		val sb = new StringBuilder
		sb.append("Occgrid [")
		sb.append(width)
		sb.append("x")
		sb.append(height)
		sb.append("]")
		sb.toString()
	}

}

/**
 * The implementation for the occgrid command to the game server.
 **/
class OccgridCommand extends Occgrid with Traversable[Array[Occupant.Occupant]] {

	private var step = 0
	private var myData: Array[Array[Occupant.Occupant]] = null;
	private var row = 0
	var width = 0
	var height = 0
	var offset = (0, 0)


	def data(x: Int)(y: Int) = myData(x)(y)


	def read(line: String) {
		step match {
			case 0 => setOffset(line)
			case 1 => createMatrix(line)
			case _ => addToMatrix(line)
		}
		step = step + 1
	}

	def addEnemies(points: Traversable[Point]) {
		addOccupants(points, Occupant.ENEMY)
	}

	def addOccupants(points: Traversable[Point], occupant: Occupant.Occupant) {
		points map (convert) foreach {
			case (x, y) =>
				myData(x)(y) = occupant
		}
	}

	private def setOffset(line: String) {
		val tokens = line.split("\\s")
		val at = tokens(0)
		val dim = tokens(1).split(",")
		val x = Integer.parseInt(dim(0))
		val y = Integer.parseInt(dim(1))
		offset = (x, y)
	}

	private def createMatrix(line: String) {
		val tokens = line.split("\\s")
		val at = tokens(0)
		val dim = tokens(1).split("x")
		width = Integer.parseInt(dim(0))
		height = Integer.parseInt(dim(1))

		//        println("(width, height) = " + (width, height))
		myData = Array.ofDim(width, height)
	}

	private def addToMatrix(line: String) {
		val rowData = myData(row)
		row = row + 1
		line.map {
			case '1' => Occupant.WALL
			case '0' => Occupant.NONE
		}.zipWithIndex.foreach {
			case (obstacle, idx) =>
				rowData(idx) = obstacle
		}
	}

	def foreach[U](f: (Array[Occupant.Occupant]) => U) {
		myData.foreach(f)
	}

	def convert(location: Point) = (location.x.intValue - offset._1, location.y.intValue - offset._2)

	override def getLocation(x: Int, y: Int) = {
		new Point(x + offset._1, y + offset._2)
	}

	override def toString() = {
		val sb = new StringBuilder
		sb.append("Occgrid [")
		sb.append(width)
		sb.append("x")
		sb.append(height)
		sb.append("]")
		sb.toString()
	}


	def print: String = {
		val sb = new StringBuilder

		myData.foreach {
			rows =>
				rows.foreach {
					case Occupant.ENEMY => sb.append("\tE")
					case Occupant.WALL => sb.append("\tW")
					case Occupant.NONE => sb.append("\t ")
					case _ => sb.append("\t!")
				}
				sb.append("\n")
		}

		sb.toString()
	}

}

object Occupant extends Enumeration {
	type Occupant = Value

	val NONE, WALL, ENEMY = Value
}

