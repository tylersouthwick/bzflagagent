package cs470.agents

import cs470.domain.Constants._
import cs470.bzrc.{DataStore, Tank}
import cs470.utils.{MovingPDController, Radian}
import cs470.movement.PotentialFieldGenerator
import cs470.domain.{Point, Vector}

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
		sleep(10000)

		loop(tank.alive && !enemies.filter(_.alive).isEmpty) {
			killEnemy()
		}

		if (tank.alive) {
			LOG.info(tank.callsign + " Is becoming a Flag Getter")
			new FlagGetter(tank.location, tank, store).apply()
		} else {
			LOG.info(tank.callsign + " [DECOY] is dead")
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

