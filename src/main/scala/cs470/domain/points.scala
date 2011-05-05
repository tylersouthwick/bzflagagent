package cs470.domain

import java.lang.Math._
import cs470.utils.Radian

object Point {
  implicit def convertTupleToPoint(t: Tuple2[Double, Double]) = new Point(t._1, t._2)
}

class Point(val x: Double, val y: Double) {

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

  def *(mult: Double) = {
    new Point(x * mult, y * mult)
  }

  def distance(goal: Point) = {
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
}

class Vector(vector: Point) {
  val x = vector.x
  val y = vector.y

  override def toString = "(" + x + ", " + y + ")"

  def /(div: Double) = {
    new Vector(x / div, y / div)
  }

  def *(mult: Double) = {
    new Vector(x * mult, y * mult)
  }

	def +(v : Vector) = new Vector(x + v.x, y + v.y)

  def magnitude = {
    vector.distance(new Point(0, 0))
  }

  def getArrowHeadPoint(point: Point) = {
    vector + point
  }

	def angle = {
		println("point: " + vector)
		val result = java.lang.Math.acos( x / y)
		new Radian(result)
	}
}

class Polygon(points: Seq[Point]) {

  def convexHull = this

  def inConvexInterior(point: Point) = false

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

  val maxDistance = {
    points(0).distance(center)
  }
}

// vim: set ts=4 sw=4 et:
