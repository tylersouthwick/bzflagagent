package cs470.movement

import java.lang.Math._
import cs470.bzrc.BzrcQueue
import cs470.domain._
import Point._

class PotentialFieldGenerator(q: BzrcQueue) {

  val enemies = q.invokeAndWait(_.othertanks)
  val obstacles = q.invokeAndWait(_.obstacles)
  val flags = q.invokeAndWait(_.flags)
  val ourColor = "blue"

  import PotentialFieldGenerator._

  def getPFVector(point: Point) = {
    val goalColor = "green"

    val fromObstacles = obstacles.foldLeft(new Point(0, 0))((total, obstacle) =>
      total + RegectivePF(point, obstacle.center, obstacle.maxDistance, 50, .9)
    )

    val fromFlags = flags.filter(_.color == goalColor).foldLeft(new Point(0,0))((total,flag) =>
      total + AttractivePF(point,flag.location, 5,70,.5)
    )

    fromObstacles + fromFlags
  }

  def AttractivePF(current: Point, goal: Point, r1: Double, s: Double, alpha: Double) = {
    val r2 = r1+s
    val as = alpha * s
    val d = current.distance(goal)
    val theta = current.getAngle(goal)

    if (d < r1)
      new Point(0, 0)
    else if (d > r2)
      new Point(as * cos(theta), as * sin(theta))
    else
      new Point(alpha * (d - r1) * cos(theta), alpha * (d - r1) * sin(theta))
  }

  def RegectivePF(current: Point, goal: Point, r1: Double, s: Double, beta: Double) = {
    val r2 = r1 + s
    val d = current.distance(goal)
    val theta = current.getAngle(goal)
    val i = 30

    if (d < r1)
      new Point(-signum(cos(theta)) * i, -signum(sin(theta)) * i)
    else if (d > r2)
      new Point(0, 0)
    else
      new Point(-beta * (r2 - d) * cos(theta), -beta * (r2 - d) * sin(theta))
  }
}

object PotentialFieldGenerator {
  val LOG = org.apache.log4j.Logger.getLogger("cs470.movement.pf")
}