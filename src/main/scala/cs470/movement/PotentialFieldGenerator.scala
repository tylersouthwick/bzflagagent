package cs470.movement

import java.lang.Math._
import cs470.bzrc.BzrcQueue
import cs470.domain._
import Point._

class PotentialFieldGenerator(q: BzrcQueue) {

  val enemies = q.invokeAndWait(_.othertanks)
  val obstacles = q.invokeAndWait(_.obstacles)
  val flags = q.invokeAndWait(_.flags)

  import PotentialFieldGenerator._

  def getPFVector(point :Point) = {
    new Vector((.5,.7))
  }

  def AttractivePF(current: Point, goal: Point, r1: Double, r2: Double, alpha: Double) = {
    val as = alpha * (r2 - r1)
    val d = current.distance(goal)
    val theta = current.getAngle(goal)

    if (d < r1)
      new Point(0, 0)
    else if (d > r2)
      new Point (as * cos(theta), as * sin(theta))
    else
      new Point(alpha * (d - r1) * cos(theta), alpha * (d - r1) * sin(theta))
  }

  def RegectivePF(current: Point, goal: Point, r1: Double, r2: Double, beta: Double) = {
    val s = (r2 - r1)
    val d = current.distance(goal)
    val theta = current.getAngle(goal)
    val i = 30

    if (d < r1)
      new Point(-signum(cos(theta)) * i, -signum(sin(theta)) * i)
    else if (d > r2)
      new Point(-beta * (r2 - d) * cos(theta), -beta * (r2 - d) * sin(theta))
    else
      new Point(0, 0)
  }
}

object PotentialFieldGenerator {
  val LOG = org.apache.log4j.Logger.getLogger("cs470.movement.pf")
}