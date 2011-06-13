package cs470.agents

import cs470.filters.KalmanFilter
import cs470.domain.Constants._
import cs470.bzrc.{RefreshableData, Enemy, DataStore, Tank}
import cs470.utils.{MovingPDController, Radian}
import cs470.movement.PotentialFieldGenerator
import cs470.domain.{Point, Vector}
import cs470.visualization.PFVisualizer
import javax.management.remote.rmi._RMIConnection_Stub

/**
 * @author tylers2
 */

object Decoy {
	val LOG = org.apache.log4j.Logger.getLogger(classOf[Decoy])
}

case class Decoy(tank : Tank, store : DataStore) extends Agent(tank, store) {
	import Decoy._

	import java.lang.Math.PI
	def apply() {
		LOG.info(tank.callsign + " is a DECOY!")
		val angle = tank.angle.radian
		if (angle >= Radian(PI / 2.0) && angle <= Radian(3.0 * PI / 4.0)) {
			tank.setSpeed(-1)
		} else {
			tank.setSpeed(1)
		}
		sleep(5000)
		//go part way to flag
		/*
		val flags = store.flags.filter(_.color != team)
		val goal = flags.last.location
		val searcher = findPath(goal)
		new MovingPDController(goal, tank, store) {
			def direction = searcher.getPathVector(tank.location)

			howClose = 200
		}.move()
		*/

		loop(tank.alive && enemies.filter(_.dead).isEmpty) {
			killEnemy()
		}

		if (tank.alive) {
			LOG.info(tank.callsign + " Is becoming a Flag Getter")
			new FlagGetter(tank.location, tank, store).apply()
		}

	}

	def enemy = enemies.getClosest(tank.location)

	def killEnemy() {
		//circle enemy
		val searcher = new PotentialFieldGenerator(store) {
			def getPathVector(point: Point) = {
				val distance = 50
				val goal = enemy.predict(500)
				val towards = AttractivePF(point, goal, distance, distance + 50, 10)
				val away = ReflectivePF(point, goal, 5, distance - 5, 30)
				val around = TangentialPF(point, goal, distance - 10, 20, 30, true)
				new Vector(towards + away + around + getFieldForObstacles(point) + getFieldForTanks(point) + randomVector * 5)
			}
		}

		/*
		val filename = new PFVisualizer {
			val pathFinder = searcher
			val name = "decoy"
			val obstacleList = store.obstacles
			val worldsize : Int = store.constants("worldsize")
			val samples = 25
			val fileName = "decoy"
			val plotTitle = "decoy"
		}.draw()
		Runtime.getRuntime.exec(Array("gnuplot", "-persist", filename))
		*/

		new MovingPDController(Point.ORIGIN, tank, store) {
			def direction = searcher.getPathVector(tank.location)

			override def getTurningSpeed(angle: Double) = .7

			override def inRange(vector: Vector) = enemy.dead
		}.move()
	}

}

case class Dalek(tank: Tank, store: DataStore) extends Agent(tank, store) {
	import cs470.agents.Dalek._

	val shotspeed : Int = constants("shotspeed")
	val bulletVelocity = shotspeed

	def shootEnemy(enemy : Enemy) {
		val futureTime = 2500
		val prediction = enemy.predict(futureTime)
		val dist = prediction - tank.location
		val angle = Radian(java.lang.Math.atan2(dist.y, dist.x))
		//				println("Moving to angle = " + angle.degree + " point@=" + prediction)
		tank.moveToAngle(angle)
		//				println("moved to position: " + prediction)
		val timeForBullet = tank.location.distance(prediction) / bulletVelocity
		val timeForEnemy = futureTime
		val timeToWait : Int = ((timeForBullet - timeForEnemy) * 1000.0).asInstanceOf[Int]
		if (timeToWait > 0) {
			LOG.info("Need to wait " + timeToWait + " bullet=" + timeForBullet + " enemy=" + timeForEnemy)
			sleep(timeToWait)
		} else {
			//					LOG.info("Tank is behind")
		}
		//tank.shoot()
	}

	def apply() {
		LOG.info(tank.callsign + " is a dalek!")
		val enemy = enemies.getClosest(tank.location)
		LOG.info(tank.callsign + " is going for " + enemy.callsign)

		sleep(2000)

		def hasLineOfSight = occgrid.hasLineOfSight(tank.location, enemy.location)
		loop (enemy.alive) {
			val goal = enemy.location
			val searcher = findPath(goal)
			var count = 0
			def checkCount = {
				if (count > 4) false else {
					count = count + 1
					true
				}
			}
			new MovingPDController(goal, tank, store) {
				def direction = searcher.getPathVector(tank.location)

				howClose = 100

				override def inRange(vector: Vector) = {
					checkCount && enemy.alive && hasLineOfSight && super.inRange(vector)
				}
			}.move()
			tank.setSpeed(0)
			tank.setAngularVelocity(0)
			shootEnemy(enemy)
		}
	}
}

object Dalek {
	val LOG = org.apache.log4j.Logger.getLogger(classOf[Dalek])
}