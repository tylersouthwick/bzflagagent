package cs470.domain

import collection.mutable.{HashMap, LinkedList}
import cs470.domain._
import Constants._
import cs470.bzrc.{RefreshableEnemies, DataStore}

/**
 * @author tylers2
 */

trait Occgrid {
  def data(x: Int)(y: Int): Occupant.Occupant

  def offset: (Int, Int)

  def width: Int

  def height: Int
}


class UsableOccgrid(resolution: Int, obstacles: Seq[Polygon],tankRadius:Double, val worldSize: Int, enemies: RefreshableEnemies) extends Occgrid with Traversable[Array[Occupant.Occupant]] {
  val alpha = math.floor(worldSize / resolution)
  val width = resolution
  val height = resolution
  val offset = (0, 0)

  private val myData : Array[Array[Occupant.Occupant]] = Array.ofDim(width, height);

  def data(x:Int)(y: Int) = myData(x)(y)

  def inGridPoint(xs: Double, ys: Double, x: Double, y: Double) = {
    if (xs <= x && x <= xs + alpha && ys <= y && y <= y + alpha) {
      true
    } else {
      false
    }
  }

  def inCircle(xs: Int, ys: Int, point: Point, theta: Double) = {
    import scala.math._
    val x = point.x
    val y = point.y
    val p1 = x + cos(theta) * tankRadius
    val p2 = y + sin(theta) * tankRadius

    inGridPoint(xs, ys, p1, p2)
  }

  def checkEnemy(x: Int, y: Int) : Occupant.Occupant = {
    import math._
    val angles = Seq(Pi / 4, 3 * Pi / 4, 5 * Pi / 4, 7 * Pi / 4)
    enemies.foreach {
      enemy =>
        angles.foreach {
          angle =>
            if (inCircle(x, y, enemy.location, angle)) return Occupant.ENEMY
        }
    }

    Occupant.NONE
  }

  def foreach[U](f: (Array[Occupant.Occupant]) => U) {
    myData.foreach(f)
  }

  def fillArray = {

    (0 to width).foreach {
      x =>
        (0 to height).foreach {
          y =>
            if(checkEnemy(x,y) == Occupant.ENEMY) {
              myData(x)(y) = Occupant.ENEMY
            } else {
              myData(x)(y) = Occupant.NONE
            }

        }
    }

    myData
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

  def addOccupants(points: Traversable[Point], occupent: Occupant.Occupant) {
    points map (convert) foreach {
      case (x, y) =>
        myData(x)(y) = occupent
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

