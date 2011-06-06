package cs470.agents

import cs470.bzrc.{DataStore, Tank}
import cs470.domain._
import cs470.utils.{Degree, Radian, Threading}
import cs470.movement.{PotentialFieldConstants, PotentialFieldGenerator, PotentialFieldsMover}
import util.Random

class KalmanPigeonsAgent(host: String, port: Int) extends Agent(host, port) with Threading {
	def run() {
		val pigeons = Seq(MovingPigeon(myTanks(1), store),
			SittingDuckPigeon(myTanks(0), store),
			NonConformingClayPigeon(myTanks(2), store))
		for (pigeon <- pigeons) {
			actor {
				pigeon.start()
			}
		}
	}

}

object KalmanPigeonsAgent extends AgentCreator {
	val LOG = org.apache.log4j.Logger.getLogger(classOf[KalmanPigeonsAgent])

	def name = "kalmanPigeons"

	def create(host: String, port: Int) = new KalmanPigeonsAgent(host, port)
}

sealed abstract class Pigeon(tank: Tank, store: DataStore) {
	val startingPosition: Point
	val LOG: org.apache.log4j.Logger

	def moveToLocation(location: Point) {
		LOG.info("Moving " + tank.callsign)
		val mytank = tank
		new PotentialFieldsMover(store) {
			def goal = location

			def path = new Vector(location - tank.location)

			val tank = mytank

			override def inRange(vector: Vector) = distance < 5

		}.moveAlongPotentialField()

		LOG.info("Done moving " + tank.callsign)
	}

	def moveToStartingLocation() {
		moveToLocation(startingPosition)
	}

	final def start() {
		moveToStartingLocation()

		while (!tank.dead) {
			aliveLoop()
		}

		LOG.info(tank.callsign + " has been killed")
	}

	def aliveLoop()
}

case class SittingDuckPigeon(tank: Tank, store: DataStore) extends Pigeon(tank, store) {
	val startingPosition = new Point(0, -100)
	val LOG = org.apache.log4j.Logger.getLogger(classOf[SittingDuckPigeon])

	override def aliveLoop() {
	}
}

case class MovingPigeon(tank: Tank, store: DataStore) extends Pigeon(tank, store) with Threading {
	val startingPosition = new Point(-100, 0)
	val LOG = org.apache.log4j.Logger.getLogger(classOf[MovingPigeon])
	val movementTime = 15000

	override def moveToStartingLocation() {
		super.moveToStartingLocation()
		tank.moveToAngle(Degree(90))
		tank.setSpeed(.5)
		sleep(movementTime / 2)
		tank.setSpeed(0)
	}

	def aliveLoop() {
		tank.setSpeed(-.8)
		sleep(movementTime)
		tank.setSpeed(.8)
		sleep(movementTime)
	}
}

case class NonConformingClayPigeon(tank: Tank, store: DataStore) extends Pigeon(tank, store) with Threading {
	val startingPosition = new Point(0, 100)
	val LOG = org.apache.log4j.Logger.getLogger(classOf[NonConformingClayPigeon])

	private val randomGenerator = new Random()

	private def randomness = randomGenerator.nextGaussian() + .5

		val angles = Seq(-1.0,-.5,0,.5,1.0)
	var count = 0

	def aliveLoop() {
		val s = randomGenerator.nextGaussian() * 2.0
		val a = 0.0
		LOG.debug("Setting speed: " + s + " and angle: " + a)
		tank.setSpeed(s)
		count = count + 1
		if(count > 3) count = 0
		tank.setAngularVelocity(angles(count))
		sleep(2000)
	}
}