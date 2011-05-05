package cs470.movement

import java.lang.Math._
import cs470.domain._
import Point._
import cs470.bzrc.{DataStore, BzrcQueue}
import java.util.Random
import java.security.SecureRandom
import cs470.utils.Degree

class pfReturnToGoal(store: DataStore, baseGoalColor: String) extends PotentialFieldGenerator(store) {

  def getPathVector(point: Point) = {
    val fromObstacles = obstacles.foldLeft(new Point(0, 0))((total, obstacle) =>
      total + RegectivePF(point, obstacle.center, obstacle.maxDistance + 5, 50, 1)
    )

    val base = bases.filter(_.color == baseGoalColor)
    val fromBase =  AttractivePF(point, base(0).points.center, 5, 50, 1)

    new Vector(fromObstacles + fromBase + randomVector)
  }
}

class pfFindFlag(store : DataStore, flagColor: String) extends PotentialFieldGenerator(store) {

  def getPathVector(point: Point) = {

    val fromObstacles = obstacles.foldLeft(new Point(0, 0))((total, obstacle) =>
      total + RegectivePF(point, obstacle.center, obstacle.maxDistance + 5, 50, 1)
    )

	  val fromEnemies =   getFieldForEnemies(point)

	  val fromTanks = store.tanks.filter(tank => tank.location != point).foldLeft(new Point(0, 0))((total, tank) =>
		  total + RegectivePF(point, tank.location, 5, 10, .2)
    )

    val fromFlags =  AttractivePF(point, flags.filter(_.color == flagColor).apply(0).location, 5, 70, .5)

    new Vector(fromObstacles + fromTanks + fromFlags + fromEnemies)
  }

}

abstract class PotentialFieldGenerator(store: DataStore) extends FindAgentPath(store) {
	val random = new Random(new java.util.Date().getTime)
	val max = .02
	def randomVector = new Point(random.nextDouble % max, random.nextDouble % max)
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

  def getFieldForEnemies(current:Point) = {
     enemies.foldLeft(new Point(0,0))((total,enemy) =>
        total + RegectivePF (current,enemy.location,3,5,.4)
     )
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

	val positive90 = Degree(90)
	val negative90 = Degree(-90)

	def TangentialPF(current: Point, goal: Point, r1: Double, s: Double, beta: Double, clockwise : Boolean) = {
		val r2 = r1 + s
		val d = current.distance(goal)
		val theta1 = current.getAngle(goal)
		val i = 30

		val theta = theta1 + {
			if (clockwise) positive90 else negative90
		}
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