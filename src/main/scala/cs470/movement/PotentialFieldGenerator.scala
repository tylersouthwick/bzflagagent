package cs470.movement

import java.lang.Math._
import cs470.domain._
import cs470.bzrc.DataStore
import java.util.Random
import cs470.utils.Degree

trait PotentialFieldConstants {
  val s = new {
    val obstacle = 50
    val base = 50
    val tanks = 10
    val flag = 70
    val enemy = 5
  }

  val alpha = new {
    val obstacle = 20
    val base = 1
    val tanks = .2
    val flag = .5
    val enemy = .4
  }

  val r = new {
    val base = 5
    val obstacle = 5
    val tanks = 5
    val flag = 5
    val enemy = 3
  }
}

object PotentialFieldConstants extends PotentialFieldConstants

import PotentialFieldConstants._

class pfReturnToGoal(store: DataStore, baseGoalColor: String) extends PotentialFieldGenerator(store) {

  val base = bases.find(_.color == baseGoalColor).get

  def getPathVector(point: Point) = {
    val toBase = AttractivePF(point, base.points.center, r.base, s.base, alpha.base)

    new Vector(toBase + buildRejectiveField(point) + randomVector)
  }
}

class pfGotoPoint(store: DataStore, goal : Point) extends PotentialFieldGenerator(store) {

	def getPathVector(point: Point) = {
		val toFlag = AttractivePF(point, goal, r.flag, s.flag, alpha.flag)

		new Vector(toFlag + buildRejectiveField(point) + randomVector)
	}

}

class pfFindFlag(store: DataStore, flagColor: String) extends PotentialFieldGenerator(store) {

  val flag = flags.find(_.color == flagColor).get

  def getPathVector(point: Point) = {
    val toFlag = AttractivePF(point, flag.location, r.flag, s.flag, alpha.flag)

    new Vector(toFlag + buildRejectiveField(point) + randomVector)
  }

}

abstract class PotentialFieldGenerator(store: DataStore) extends FindAgentPath(store) {
  def randomVector = PotentialFieldGenerator.randomVector

  def AttractivePF2(current: Point, goal: Point, r1: Double, s: Double, alpha: Double) = {
    val r2 = r1 + s
    val as = alpha * s
    val d = current.distance(goal)
    val theta = current.getAngle(goal)

    if (d < r1)
      new Point(0, 0)
    else if (d > r2)
      new Point(0, 0)
    else
      new Point(alpha * (d - r1) * cos(theta), alpha * (d - r1) * sin(theta))
  }

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

  def buildRejectiveField(current: Point) = getFieldForEnemies(current) + getFieldForTanks(current) + getFieldForObstacles(current)

  def getFieldForEnemies(current: Point) = {
    enemies.foldLeft(new Point(0, 0))((total, enemy) =>
      total + RegectivePF(current, enemy.location, r.enemy, s.enemy, alpha.enemy)
    )
  }

  def getFieldForObstacles(point: Point, s: Double, alpha: Double): Point = {
    obstacles.foldLeft(new Point(0, 0))((total, obstacle) =>
      total + ReflectivePF(point, obstacle.center, obstacle.maxDistance + r.obstacle, s, alpha)
    )
  }

  def getFieldForObstacles(point: Point): Point = getFieldForObstacles(point, s.obstacle, alpha.obstacle)

  def getFieldForObstaclesTangential(point: Point, clockwise: Boolean) = {
    obstacles.foldLeft(new Point(0, 0))((total, obstacle) =>
      total + TangentialPF(point, obstacle.center, obstacle.maxDistance + r.obstacle, s.obstacle, alpha.obstacle, clockwise)
    )
  }

  def getFieldForTanks(point: Point) = {
    tanks.filter(tank => tank.location != point).foldLeft(new Point(0, 0))((total, tank) =>
      total + RegectivePF(point, tank.location, r.tanks, s.tanks, alpha.tanks)
    )
  }

  def RegectivePF(current: Point, goal: Point, r1: Double, s: Double, beta: Double) = {
    ReflectivePF(current, goal, r1, s, beta)
  }

  def ReflectivePF(current: Point, goal: Point, r1: Double, s: Double, beta: Double) = {
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
	private val s2 = .1
val scale = 5
	def randomVector = new Point(s2 + scale * scala.util.Random.nextGaussian, s2 + scale * scala.util.Random.nextGaussian)

	def ReflectivePF(current: Point, goal: Point, r1: Double, s: Double, beta: Double) = {
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
