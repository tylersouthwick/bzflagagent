package cs470.bzrc

import cs470.BzrcQueue
import java.util.concurrent.{TimeUnit, Executors}
import java.util.concurrent.locks.ReentrantLock
import cs470.domain.MyTank

class RefreshableTanks(queue : BzrcQueue) {
	private var tanks : Seq[MyTank] = Seq[MyTank]()
	private val LOG = org.apache.log4j.Logger.getLogger(classOf[RefreshableTanks])

	private val locker = new ReentrantLock
	private def doLock(callback : => Unit) {
		locker.lock()
		try {
			callback
		} finally {
			locker.unlock()
		}
	}

	private def lock[T](callback : => T) : T = {
		locker.lock()
		try {
			callback
		} finally {
			locker.unlock()
		}
	}

	Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(new Runnable {
		def run() {
			LOG.debug("reloading tanks")
			val myTanks = queue.invokeAndWait(_.mytanks)
			LOG.debug("reload tanks")
			doLock {
				tanks = myTanks
			}
		}
	}, 0, 100, TimeUnit.MILLISECONDS)

	def get(id : Int) = new MyTank {
		private def tank : MyTank = lock {
			tanks.filter(_.id == id).apply(0)
		}

		def angvel = tank.angvel
		def xy = tank.xy
		def vx = tank.vx
		def angle = tank.angle
		def location = tank.location
		def flag = tank.flag
		def timeToReload = tank.timeToReload
		def shotsAvailable = tank.shotsAvailable
		def status = tank.status
		def callsign = tank.callsign
		def id = tank.id
	}

}
