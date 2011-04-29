package cs470.bzrc

import java.util.concurrent.{TimeUnit, Executors}
import java.util.concurrent.locks.ReentrantLock

object RefreshableData {
	private val LOG = org.apache.log4j.Logger.getLogger(classOf[RefreshableData])
	protected[RefreshableData] val executor = Executors.newScheduledThreadPool(5)
}

trait RefreshableData {
	private val locker = new ReentrantLock

	protected def doLock(callback : => Unit) {
		locker.lock()
		try {
			callback
		} finally {
			locker.unlock()
		}
	}

	protected def lock[T](callback : => T) : T = {
		locker.lock()
		try {
			callback
		} finally {
			locker.unlock()
		}
	}

	/**
	 * Schedules the task for every 100 Milliseconds
	 */
	def schedule (callback : => Unit) {
		callback
		RefreshableData.executor.scheduleWithFixedDelay(new Runnable {
			def run() {
				callback
			}
		}, 0, 500, TimeUnit.MILLISECONDS)
	}
}
