package cs470.bzrc

import cs470.domain.{Point, OtherTank}
import cs470.filters.KalmanFilter
import cs470.utils.{Threading, Angle}
import collection.mutable.HashMap

class RefreshableEnemies(queue : BzrcQueue) extends RefreshableData[OtherTank, Enemy](queue) {

	def findData(data: BzData) = data.othertanks
	private val filters = new HashMap[String, KalmanFilter]


	override protected def loaded(enemies : Traversable[Enemy]) {
		for (enemy <- enemies) {
			filters += enemy.callsign -> new KalmanFilter(enemy.callsign, enemy.reportedLocation)
		}
		new Threading {
			actor {
				loop {
					filters.foreach(_._2.update())
				}
				sleep(500)
			}
		}
	}

	def getClosest(location : Point) = {
		foldLeft(null : Enemy){(prev,enemy) =>
			if(prev == null) enemy
			else {
				if(enemy.location.distance(location) < prev.location.distance(location)) enemy
				else prev
			}
		}
	}


	protected def convert(f: OtherTank) = new Enemy {
		def tank = findItem(_.callsign == callsign)

		def angle = tank.angle
		def reportedLocation = tank.location
		def flag = tank.flag
		def status = tank.status
		def color = tank.color

		val callsign = f.callsign

		private def filter = filters(callsign)
		def location = filter.position

		def predict(time: Int) = filter.predict(time.asInstanceOf[Double]/1000.0)
	}
}

trait Enemy {
	val callsign : String
	def color : String
	def status : String
	def flag : String
	def reportedLocation : Point
	def location : Point
	def angle : Angle
	def predict(time : Int) : Point

	def alive = "alive" == status
	def dead = "dead" == status
}