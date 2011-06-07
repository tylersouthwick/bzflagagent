package cs470.agents

import cs470.utils._
import cs470.filters.KalmanFilter
import cs470.bzrc.{RefreshableData, Enemy}
import cs470.domain._
import Constants._
import java.util.concurrent.CountDownLatch

class KalmanAgent(host: String, port: Int) extends Agent(host, port) with Threading {
	import KalmanAgent._
	def time = new java.util.Date().getTime

val shotspeed : Int = constants("shotspeed")
val bulletVelocity = shotspeed

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

		for (x <- 0 to 5) {
			filter.update()
			RefreshableData.waitForNewData()
		}

		actor {
			while (enemy.alive) {
				filter.update()
				if (LOG.isDebugEnabled) {
					LOG.debug("mu: \n" + filter.mu)
					LOG.debug("confidence: \n" + filter.sigma)
				}
					//println("mu: \n" + filter.mu)
					//println("covariance: \n" + filter.sigma)
				RefreshableData.waitForNewData()
			}
			latch.countDown()
			LOG.info("Done updating for " + enemy.callsign)
		}
	
		actor {
			while (enemy.alive) {
				val start = time
				val futureTime : Double = 6000
				val prediction = filter.predict(futureTime / 1000)
				val dist = prediction - tank.location
				val angle = Radian(java.lang.Math.atan2(dist.y, dist.x))
				println("Moving to angle = " + angle.degree + " point@=" + prediction)
				tank.moveToAngle(angle)
				filter.update()
				val timeForBullet = tank.location.distance(prediction) / bulletVelocity
				val timeForEnemy = prediction.distance(filter.position) / filter.velocity
				val timeToWait : Int = ((timeForBullet - timeForEnemy) * 1000.0).asInstanceOf[Int]
				if (timeToWait > 0) {
					LOG.info("Need to wait " + timeToWait + " bullet=" + timeForBullet + " enemy=" + timeForEnemy)
					sleep(timeToWait)
				}
				tank.shoot()
				LOG.info("Shooting " + enemy.callsign + " ->" + prediction + " @" + enemy.location)
				LOG.debug("angle: " + (tank.angle.degree, angle.degree))
				RefreshableData.waitForNewData()
			}
			latch.countDown()
			LOG.info("Done shooting " + enemy.callsign)
		}
	
		while (latch.getCount > 0) {}
	
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
