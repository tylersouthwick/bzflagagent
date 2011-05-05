package cs470.movement

import java.lang.Math._
import cs470.domain._
import Point._
import cs470.bzrc.{DataStore, BzrcQueue}

class pfReturnToGoal(store: DataStore, baseGoalColor: String) extends PotentialFieldGenerator(store) {

  def getPathVector(point: Point) = {
    val fromObstacles = obstacles.foldLeft(new Point(0, 0))((total, obstacle) =>
      total + RegectivePF(point, obstacle.center, obstacle.maxDistance + 5, 50, 1.2)
    )

    val base = bases.filter(_.color == baseGoalColor)
    val fromBase =  AttractivePF(point, base(0).points.center, 5, 70, .5)

    new Vector(fromObstacles + fromBase)
  }
}

class pfFindFlag(store : DataStore, flagColor: String) extends PotentialFieldGenerator(store) {

  def getPathVector(point: Point) = {

    val fromObstacles = obstacles.foldLeft(new Point(0, 0))((total, obstacle) =>
      total + RegectivePF(point, obstacle.center, obstacle.maxDistance + 5, 50, 1.2)
    )

	  /*
	  val enemies = enemies.foldLeft(new Point(0, 0))((total, obstacle) =>
		  total + RegectivePF(point, obstacle.center, obstacle.maxDistance + 5, 50, 1.2)
	  )
	  */

	  val fromTanks = store.tanks.foldLeft(new Point(0, 0))((total, tank) =>
		  total //+ RegectivePF(point, tank.location, 5, 5, .8)
	  )

    val fromFlags =  AttractivePF(point, flags.filter(_.color == flagColor).apply(0).location, 5, 70, .5)

    new Vector(fromObstacles + fromTanks + fromFlags)
  }

}

abstract class PotentialFieldGenerator(store: DataStore) extends FindAgentPath(store) {
  def AttractivePF(current: Point, goal: Point, r1: Double, s: Double, alpha: Double) = {
    val r2 = r1 + s
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