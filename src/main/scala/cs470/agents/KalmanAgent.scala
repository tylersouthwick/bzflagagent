package cs470.agents

import cs470.utils._
import cs470.filters.KalmanFilter
import cs470.bzrc.{RefreshableData, Enemy}
import cs470.domain.Point

class KalmanAgent(host: String, port: Int) extends Agent(host, port) with Threading {
	import KalmanAgent._
	def time = new java.util.Date().getTime

	def run() {
		val tank = myTanks(0)

		implicit object ClosestEnemy extends Ordering[Enemy] {
			def compare(x: Enemy, y: Enemy) = {
				x.location.distance(tank.location) compareTo y.location.distance(tank.location)
			}
		}

		enemies.filter(_.status == "alive").foreach{enemy =>
			val filter = KalmanFilter(enemy)
			LOG.info("Aiming for " + enemy.callsign)
			var filteringDone = false
			var shootingDone = false

			actor {
				while (enemy.status == "alive") {
					filter.update()
					LOG.debug("mu: \n" + filter.mu)
					LOG.debug("confidence: \n" + filter.sigma)
					RefreshableData.waitForNewData()
				}
				filteringDone = true
				LOG.info("Done updating for " + enemy.callsign)
			}

			val shooting = actor {
				while (enemy.status == "alive") {
					val start = time
					val futureTime = 1000
					val prediction = filter.predict(futureTime / 1000)
					val dist = prediction - tank.location
					val angle = Radian(java.lang.Math.atan2(dist.y, dist.x))
					tank.moveToAngle(angle)
					val timeToWait = futureTime - (time - start)
					if (timeToWait > 0) {
						sleep(timeToWait)
					}
					LOG.info("angle: " + (tank.angle.degree, angle.degree))
					tank.shoot()
				}
				shootingDone = true
				LOG.info("Done shooting " + enemy.callsign)
			}

			while (!(filteringDone && shootingDone)) {}

			LOG.info("Successfully killed " + enemy.callsign)
		}

		LOG.info("Kalman Agent Done")
	}
}

object KalmanAgent extends AgentCreator {
	val LOG = org.apache.log4j.Logger.getLogger(classOf[KalmanAgent])

	def name = "kalman"

	def create(host: String, port: Int) = new KalmanAgent(host, port)

}
