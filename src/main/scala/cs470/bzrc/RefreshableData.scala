package cs470.bzrc

import java.util.concurrent.{TimeUnit, Executors}
import java.util.concurrent.locks.ReentrantLock
import java.util.Date
import collection.immutable.List

object RefreshableData {
	private val LOG = org.apache.log4j.Logger.getLogger("cs470.bzrc.RefreshableData")
	private val executor = Executors.newScheduledThreadPool(5)
	private val waitingObject = new Object
	protected[RefreshableData] var loaders = Seq[() => Unit]()

	def time = (new Date).getTime
	/**
	 * Schedules the task for every 100 Milliseconds
	 */
	def schedule (callback : () => Unit) {
		loaders = callback +: loaders
	}

	private def runLoaders() {
		loaders.foreach(callback =>
				callback()
		)
	}

	RefreshableData.executor.scheduleWithFixedDelay(new Runnable {
		def run() {
			val start = time
			runLoaders()
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

	def waitForNewData() {
		waitingObject.synchronized{
			try {
				waitingObject.wait()
			} catch {
				case t : Throwable => println("error WAIT: " + t)
			}
		}
	}
}

abstract class RefreshableData[F, T](queue : BzrcQueue) extends Traversable[T] {
	import RefreshableData.{LOG,schedule}
	private val locker = new ReentrantLock
	private var buffer : Seq[F] = null

	lazy val availableData = lock { buffer.map(convert(_)) }

	protected def loadData(con : BzFlagConnection) : Seq[F]
	protected def convert(f : F) : T

	protected def findItem(callback : F => Boolean) = lock {
		buffer.filter(callback).apply(0)
	}

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

	final def foreach[U](f: (T) => U) {
		availableData.foreach(t => f(t))
	}

	final def apply(index : Int) = availableData.apply(index)

	final def waitForNewData() {
		RefreshableData.waitForNewData()
	}

	schedule({ () =>
		LOG.debug("reloading buffer")
		val myBuffer = queue.invokeAndWait(loadData)
		LOG.debug("reload buffer")
		doLock {
			buffer = myBuffer
		}
	})
}
