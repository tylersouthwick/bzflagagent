package cs470.movement

import java.lang.Math._
import cs470.domain._
import cs470.bzrc.DataStore
import java.util.Random
import cs470.utils.Degree

object PotentialFieldConstants {
	val s = new {
		val obstacle = 50
		val base = 50
		val tanks = 10
		val flag = 70
	}

	val alpha = new {
		val obstacle = 1
		val base = 1
		val tanks = .2
		val flag = .5
	}

	val r = new {
		val base = 5
		val obstacle = 5
		val tanks = 5
		val flag = 5
	}
}

import PotentialFieldConstants._
class pfReturnToGoal(store: DataStore, baseGoalColor: String) extends PotentialFieldGenerator(store) {

	def getPathVector(point: Point) = {
		val fromObstacles = obstacles.foldLeft(new Point(0, 0))((total, obstacle) =>
			total + RegectivePF(point, obstacle.center, obstacle.maxDistance + r.obstacle, s.obstacle, alpha.obstacle)
		)

		val base = bases.filter(_.color == baseGoalColor)
		val fromBase = AttractivePF(point, base(0).points.center, r.base, s.base, alpha.base)

		new Vector(fromObstacles + fromBase + randomVector)
	}
}

class pfFindFlag(store: DataStore, flagColor: String) extends PotentialFieldGenerator(store) {

	def getPathVector(point: Point) = {

		val fromObstacles = obstacles.foldLeft(new Point(0, 0))((total, obstacle) =>
			total + RegectivePF(point, obstacle.center, obstacle.maxDistance + r.obstacle , s.base, alpha.obstacle)
		)

		val fromEnemies = getFieldForEnemies(point)

		val fromTanks = store.tanks.filter(tank => tank.location != point).foldLeft(new Point(0, 0))((total, tank) =>
			total + RegectivePF(point, tank.location, r.tanks, s.tanks, alpha.tanks)
		)

		val flag = flags.filter(_.color == flagColor).apply(0)
		val fromFlags = AttractivePF(point, flag.location, r.flag, s.flag, alpha.flag)

		new Vector(fromObstacles + fromTanks + fromFlags + fromEnemies + randomVector)
	}

}

abstract class PotentialFieldGenerator(store: DataStore) extends FindAgentPath(store) {
	private val random = new Random(new java.util.Date().getTime)
	private val s2 = .1
	def randomVector = new Point(s2 + scala.util.Random.nextGaussian, s2 + scala.util.Random.nextGaussian)

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

	def getFieldForEnemies(current: Point) = {
		enemies.foldLeft(new Point(0, 0))((total, enemy) =>
			total + RegectivePF(current, enemy.location, 3, 5, .4)
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

	def TangentialPF(current: Point, goal: Point, r1: Double, s: Double, beta: Double, clockwise: Boolean) = {
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