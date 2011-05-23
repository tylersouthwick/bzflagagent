package cs470.domain

import collection.mutable.{HashMap, LinkedList}
import cs470.domain._
import Constants._
import cs470.bzrc.{RefreshableEnemies, DataStore}

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

  def getLocation(x: Int, y: Int) : Point
}


class UsableOccgrid(resolution: Int, obstacles: Seq[Polygon], tankRadius: Double, val worldSize: Int, enemies: RefreshableEnemies) extends Occgrid with Traversable[Array[Occupant.Occupant]] {
  val alpha = math.floor(worldSize / resolution)
  val width = resolution
  val height = resolution
  val offset: (Int, Int) = (resolution / 2, resolution / 2)

  private var myData: Array[Array[Occupant.Occupant]] = Array.ofDim(width, height);

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
    val t:(Int,Int) = (location.x.intValue / alpha + offset._1, offset._2 + location.y.intValue / alpha)
//    Console.out.println(t)
    t
  }

  override def getLocation(x: Int, y: Int) = {
    new Point((x- offset._1) * alpha, (-y+ offset._2) * alpha)
  }

  def fillArray = {

    (0 to width - 1).foreach {
      x =>
        (0 to height - 1).foreach {
          y =>
            val tmp = getLocation(x, y)
            if (checkEnemy(tmp.x, tmp.y)) {
              myData(y)(x) = Occupant.ENEMY
            } else if (checkObstacle(tmp.x, tmp.y)) {
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
}

object Occupant extends Enumeration {
  type Occupant = Value

  val NONE, WALL, ENEMY = Value
}

