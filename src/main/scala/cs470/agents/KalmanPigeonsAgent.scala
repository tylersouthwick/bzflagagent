package cs470.agents

import cs470.utils.Threading
import cs470.movement.PotentialFieldsMover
import cs470.bzrc.{DataStore, Tank}
import cs470.domain._

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

	def start() {
		moveToLocation(startingPosition)
	}
}

case class SittingDuckPigeon(tank : Tank, store : DataStore) extends Pigeon(tank, store) {
	val startingPosition = new Point(0, -100)
}

case class MovingPigeon(tank : Tank, store : DataStore) extends Pigeon(tank, store) with Threading {
	val startingPosition = new Point(-100, 0)

	override def start() {
		super.start()
		loop {
			moveToLocation(new Point(-100, 100))
			moveToLocation(new Point(-100, -100))
		}
	}
}

case class NonConformingClayPigeon(tank : Tank, store : DataStore) extends Pigeon(tank, store) {
	val startingPosition = new Point(0, 100)
}