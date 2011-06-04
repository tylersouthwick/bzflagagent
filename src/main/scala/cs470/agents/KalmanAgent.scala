package cs470.agents

import cs470.utils._
import cs470.filters.KalmanFilter
import cs470.bzrc.{RefreshableData, Enemy}

class KalmanAgent(host: String, port: Int) extends Agent(host, port) with Threading {
	def run() {
		val tank = myTanks(0)

		implicit object ClosestEnemy extends Ordering[Enemy] {
			def compare(x: Enemy, y: Enemy) = {
				x.location.distance(tank.location) compareTo y.location.distance(tank.location)
			}
		}

		val enemy = enemies.filter(_.color == "green").find(_.callsign == "green0").get
		val filter = KalmanFilter(enemy)

		loop {
			filter.update()
			println("mu: ")
			println(filter.mu)
			println("confidence: ")
			println(filter.sigma)

			sleep(500)
			//RefreshableData.waitForNewData()
		}
	}

}

object KalmanAgent extends AgentCreator {
	val LOG = org.apache.log4j.Logger.getLogger(classOf[ScoutAgent])

	def name = "kalman"

	def create(host: String, port: Int) = new KalmanAgent(host, port)

}
