package cs470.agents

import cs470.bzrc.{DataStore, Tank}
import cs470.domain._
import cs470.utils.{Degree, Radian, Threading}
import cs470.movement.{PotentialFieldConstants, PotentialFieldGenerator, PotentialFieldsMover}

class KalmanPigeonsAgent(host: String, port: Int) extends Agent(host, port) with Threading {
	def run() {
		val pigeons = Seq(MovingPigeon(myTanks(0), store),
						SittingDuckPigeon(myTanks(1), store),
						NonConformingClayPigeon(myTanks(2), store))
		for (pigeon <- pigeons) {
			actor {
				pigeon.start()
			}
		}
	}

}

object KalmanPigeonsAgent extends AgentCreator {
	val LOG = org.apache.log4j.Logger.getLogger(classOf[ScoutAgent])

	def name = "kalmanPigeons"

	def create(host: String, port: Int) = new KalmanPigeonsAgent(host, port)
}

sealed abstract class Pigeon(tank : Tank, store : DataStore) {
	val startingPosition : Point

	def moveToLocation(location : Point) {
		println("moving pigeon :: " + tank.callsign)
		val mytank = tank
		new PotentialFieldsMover(store) {
			def goal = location

			def path = new Vector(location - tank.location)

			val tank = mytank

			override def inRange(vector: Vector) = distance < 5

		}.moveAlongPotentialField()
		println("done moving pigeon :: " + tank.callsign)
	}

	def moveToStartingLocation() {
		moveToLocation(startingPosition)
	}

	final def start() {
		moveToStartingLocation()

		while (!tank.dead) {
			aliveLoop()
		}
	}

	def aliveLoop()
}

case class SittingDuckPigeon(tank : Tank, store : DataStore) extends Pigeon(tank, store) {
	val startingPosition = new Point(0, -100)

	override def aliveLoop() {
	}
}

case class MovingPigeon(tank : Tank, store : DataStore) extends Pigeon(tank, store) with Threading {
	val startingPosition = new Point(-100, 0)

	val movementTime = 15000
	override def moveToStartingLocation() {
		super.moveToStartingLocation()
		tank.moveToAngle(Degree(90))
		tank.setSpeed(1)
		sleep(movementTime / 2)
		tank.setSpeed(0)
	}

	def aliveLoop() {
		tank.setSpeed(-1)
		sleep(movementTime)
		tank.setSpeed(1)
		sleep(movementTime)
	}
}

case class NonConformingClayPigeon(tank : Tank, store : DataStore) extends Pigeon(tank, store) {
	val startingPosition = new Point(0, 100)

	override def aliveLoop() {
	}
}