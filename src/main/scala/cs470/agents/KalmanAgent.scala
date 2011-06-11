package cs470.agents

import cs470.utils._
import cs470.filters.KalmanFilter
import cs470.domain._
import Constants._
import java.util.concurrent.CountDownLatch
import cs470.bzrc.{Tank, DataStore, RefreshableData, Enemy}

class KalmanAgent(tank : Tank, store : DataStore) extends Agent(tank, store) with Threading {
	import KalmanAgent._
	def time = new java.util.Date().getTime

val shotspeed : Int = constants("shotspeed")
val bulletVelocity = shotspeed

	def apply() {
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
				//	println("mu: \n" + filter.mu)
				//	println("covariance: \n" + filter.sigma)
				RefreshableData.waitForNewData()
				//sleep(500)
			}
			latch.countDown()
			LOG.info("Done updating for " + enemy.callsign)
		}
	
		actor {
			while (enemy.alive) {
				val start = time
				val futureTime : Double = 2500
				val prediction = filter.predict(futureTime / 1000.0)
				val dist = prediction - tank.location
				val angle = Radian(java.lang.Math.atan2(dist.y, dist.x))
//				println("Moving to angle = " + angle.degree + " point@=" + prediction)
				tank.moveToAngle(angle)
//				println("moved to position: " + prediction)
				filter.update()
				val timeForBullet = tank.location.distance(prediction) / bulletVelocity
				val timeForEnemy = futureTime
				val timeToWait : Int = ((timeForBullet - timeForEnemy) * 1000.0).asInstanceOf[Int]
				if (timeToWait > 0) {
					LOG.info("Need to wait " + timeToWait + " bullet=" + timeForBullet + " enemy=" + timeForEnemy)
					sleep(timeToWait)
				} else {
//					LOG.info("Tank is behind")
				}
				tank.shoot()
//				LOG.info("Shooting " + enemy.callsign + " ->" + prediction + " @" + enemy.location)
//				LOG.debug("angle: " + (tank.angle.degree, angle.degree))
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

object KalmanAgent {
	val LOG = org.apache.log4j.Logger.getLogger(classOf[KalmanAgent])
}
