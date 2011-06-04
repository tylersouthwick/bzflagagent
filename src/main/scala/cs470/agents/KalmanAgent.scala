package cs470.agents

import cs470.utils._
import cs470.filters.KalmanFilter
import cs470.bzrc.{RefreshableData, Enemy}
import cs470.domain.Point

class KalmanAgent(host: String, port: Int) extends Agent(host, port) with Threading {
	def time = new java.util.Date().getTime

	def run() {
		val tank = myTanks(0)

		implicit object ClosestEnemy extends Ordering[Enemy] {
			def compare(x: Enemy, y: Enemy) = {
				x.location.distance(tank.location) compareTo y.location.distance(tank.location)
			}
		}

		val enemy = enemies.filter(_.color == "green").find(_.callsign == "green0").get
		val filter = KalmanFilter(enemy)

		actor {
		loop {
			filter.update()
			println("mu: ")
			println(filter.mu)
			println("confidence: ")
			println(filter.sigma)

			RefreshableData.waitForNewData()
		}
		}

		actor {
		loop {
			val start = time
			val futureTime = 1000
			val prediction = filter.predict(futureTime/1000)
			val dist = prediction - tank.location
			val angle = Radian(java.lang.Math.atan2(dist.y,dist.x))
			tank.moveToAngle(angle)
			val timeToWait = futureTime - (time - start)
			if (timeToWait > 0) {
				sleep(timeToWait)
			}
			println("angle: " + (tank.angle.degree, angle.degree))
			tank.shoot()
		}
		}
	}

}

object KalmanAgent extends AgentCreator {
	val LOG = org.apache.log4j.Logger.getLogger(classOf[ScoutAgent])

	def name = "kalman"

	def create(host: String, port: Int) = new KalmanAgent(host, port)

}
