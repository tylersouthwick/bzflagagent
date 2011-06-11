package cs470.agents

import cs470.filters.KalmanFilter
import cs470.domain.Constants._
import cs470.bzrc.{RefreshableData, Enemy, DataStore, Tank}
import cs470.utils.{MovingPDController, Radian}
import cs470.movement.PotentialFieldGenerator
import cs470.domain.{Point, Vector}
import cs470.visualization.PFVisualizer

/**
 * @author tylers2
 */

object Decoy {
	val LOG = org.apache.log4j.Logger.getLogger(classOf[Decoy])
}

case class Decoy(dalek : Dalek,  tank : Tank, store : DataStore) extends Agent(tank, store) {
	import Decoy._

	def apply() {
		LOG.info(tank.callsign + " is a DECOY!")
		sleep(2000)
		val enemy = store.enemies(0)

		//circle enemy
		def searcher = new PotentialFieldGenerator(store) {
			def getPathVector(point: Point) = {
				val distance = 50
				val goal = enemy.location
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
			override def inRange(vector: Vector) = false
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