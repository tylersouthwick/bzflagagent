package cs470.domain

import java.lang.Math._

class Point(val x: Double,val y: Double) {

  override def toString = "(" + x + ", " + y + ")"

  def -(other: Point) = {
    new Point(x - other.x, y - other.y)
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

class Polygon(points: Seq[Point]) {

  def convexHull = this

  def inConvexInterior(point: Point) = false
}

// vim: set ts=4 sw=4 et:
