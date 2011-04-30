package cs470.bzrc

import java.util.concurrent.{TimeUnit, Executors}
import java.util.concurrent.locks.ReentrantLock
import java.util.Date

object RefreshableData {
	private val LOG = org.apache.log4j.Logger.getLogger("cs470.bzrc.RefreshableData")
	protected[RefreshableData] val executor = Executors.newScheduledThreadPool(5)
}

trait RefreshableData[T] extends Traversable[T] {
	private val locker = new ReentrantLock
	import RefreshableData.LOG
	private val waitingObject = new Object

	protected def availableData : Seq[T]

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

	def time = (new Date).getTime
	/**
	 * Schedules the task for every 100 Milliseconds
	 */
	def schedule (callback : => Unit) {
		callback
		RefreshableData.executor.scheduleWithFixedDelay(new Runnable {
			def run() {
				val start = time
				callback
				val end = time
				LOG.debug("Scheduled task took " + (end - start) + "ms")
				waitingObject.synchronized{
					try {
						waitingObject.notifyAll()
					} catch {
						case t : Throwable => {
							println("error notifyAll: " + t)
						}
					}
				}
			}
		}, 0, 50, TimeUnit.MILLISECONDS)
	}

	final def foreach[U](f: (T) => U) {
		availableData.foreach(t => f(t))
	}

	final def apply(index : Int) = availableData.apply(index)

	final def waitForNewData() {
		waitingObject.synchronized{
			try {
				waitingObject.wait()
			} catch {
				case t : Throwable => println("error WAIT: " + t)
			}
		}
	}
}
