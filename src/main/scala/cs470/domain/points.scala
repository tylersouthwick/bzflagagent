package cs470.domain

import java.lang.Math._
import cs470.utils.Radian

object Point {
  implicit def convertTupleToPoint(t: Tuple2[Double, Double]) = new Point(t._1, t._2)
}

class Point(val x: Double, val y: Double) {

  def this(t: (Int, Int)) {
    this (t._1, t._2)
  }

  override def toString = "(" + x + ", " + y + ")"

  def -(other: Point) = {
    new Point(x - other.x, y - other.y)
  }

  def +(other: Point) = {
    new Point(x + other.x, y + other.y)
  }

  def /(div: Double) = {
    new Point(x / div, y / div)
  }

  def *(mult: Int) : Point = *(mult.asInstanceOf[Double])

  def *(mult: Double) = {
    new Point(x * mult, y * mult)
  }

  def *(mult: Point) = {
    x * mult.x + y * mult.y
  }

  def distance(goal: Point) : Double = {
    val dx = goal.x - x
    val dy = goal.y - y

    sqrt(dx * dx + dy * dy)
  }

  def getAngle(goal: Point) = {
    val dx = goal.x - x
    val dy = goal.y - y

    atan2(dy, dx)
  }

  def magnitude = {
    distance(new Point(0, 0))
  }

  def perpendicular = new Point(-y, x)
}

class Vector(val vector: Point) {
  val x = vector.x
  val y = vector.y

  override def toString = "(" + x + ", " + y + ")"

  def /(div: Double) = {
    new Vector(x / div, y / div)
  }

  def *(mult: Double) = {
    new Vector(x * mult, y * mult)
  }

  def +(v: Vector) = new Vector(x + v.x, y + v.y)

  def magnitude = {
    vector.distance(new Point(0, 0))
  }

  def getArrowHeadPoint(point: Point) = {
    vector + point
  }

  def angle = new Radian(java.lang.Math.atan2(y, x))
}

class Polygon(points: Seq[Point]) {

  def convexHull = this

  def inConvexInterior(point: Point) = false

  val vertices = points

  val edges = points.zipWithIndex.map {
    case (point, idx) =>
      if (idx + 1 < points.size)
        (point, points(idx + 1))
      else
        (point, points(0))
  }

  val center = {
    (points.foldLeft(new Point(0, 0))((a, b) => a + b)) / points.size
  }

  val topLeft = {
    points.foldLeft(null: Point) {
      (tl, point) =>
        if (tl == null) {
          point
        } else if (point.x < tl.x || point.y > tl.y) {
          point
        } else {
          tl
        }
    }
  }
  val bottomRight = {
    points.foldLeft(null: Point) {
      (tl, point) =>
        if (tl == null) {
          point
        } else if (point.x > tl.x || point.y < tl.y) {
          point
        } else {
          tl
        }
    }
  }

	def contains(p : Point) : Boolean = contains(p.x, p.y)

	def contains(x: Double, y: Double) : Boolean = {
		x > topLeft.x && x < bottomRight.x && y < topLeft.y && y > bottomRight.y
	}

  val maxDistance = {
    points(0).distance(center)
  }

    override def toString() = points.foldLeft(new StringBuilder) { (sb, point) => sb.append(point)}.toString()

}

// vim: set ts=4 sw=4 et:
