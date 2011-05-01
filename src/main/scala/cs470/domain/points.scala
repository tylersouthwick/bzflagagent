package cs470.domain

import java.lang.Math._

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

}

class Vector(vector: Point) {
  val x = vector.x
  val y = vector.y

  override def toString = "(" + x + ", " + y + ")"

  def magnitude = {
    vector.distance(new Point(0, 0))
  }

  def getArrowHeadPoint(point: Point) = {
    vector + point
  }
}

class Polygon(points: Seq[Point]) {

  def convexHull = this

  def inConvexInterior(point: Point) = false

  val edges = points.zipWithIndex.map {
    case (point, idx) =>
      if (idx + 1 < points.size)
        (point, points.apply(idx + 1))
      else
        (point, points.apply(0))
  }
}

// vim: set ts=4 sw=4 et:
