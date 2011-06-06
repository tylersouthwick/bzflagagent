package cs470.agents

import cs470.utils._
import cs470.filters.KalmanFilter
import cs470.bzrc.{RefreshableData, Enemy}
import cs470.domain.Point
import java.util.concurrent.CountDownLatch

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

		enemies.filter(_.alive).foreach{enemy =>
			val filter = KalmanFilter(enemy)
			LOG.info("Aiming for " + enemy.callsign)
			val latch = new CountDownLatch(2)

			(1 to 5) foreach { x=>
				filter.update()
				RefreshableData.waitForNewData()
			}

				while (enemy.alive) {
			(1 to 3) foreach { x =>
				filter.update()
				RefreshableData.waitForNewData()
			}
					if (LOG.isDebugEnabled) {
						LOG.debug("mu: \n" + filter.mu)
						LOG.debug("confidence: \n" + filter.sigma)
					}
					val start = time
					val futureTime = 1000
					val prediction = filter.predict(futureTime / 1000)
					println("location: " + enemy.location)
					println("prediction: " + prediction)
					val dist = prediction - tank.location
					val angle = Radian(java.lang.Math.atan2(dist.y, dist.x))
					tank.moveToAngle(angle)
					//val timeToWait = futureTime - (time - start)
					//println("Waiting: " + timeToWait)
					LOG.debug("angle: " + (tank.angle.degree, angle.degree))
					LOG.info("Shooting " + enemy.callsign)
					println("location: " + enemy.location)
					tank.shoot()
					sleep(50)
					tank.shoot()
					sleep(50)
					tank.shoot()
					sleep(50)
					tank.shoot()
					sleep(50)
					tank.shoot()
					RefreshableData.waitForNewData()
				}

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
