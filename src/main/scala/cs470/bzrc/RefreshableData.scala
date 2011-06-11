package cs470.bzrc

import java.util.concurrent.{TimeUnit, Executors}
import java.util.concurrent.locks.ReentrantLock
import java.util.Date

object RefreshableData {
	private val LOG = org.apache.log4j.Logger.getLogger("cs470.bzrc.RefreshableData")
	private val executor = Executors.newScheduledThreadPool(5)
	private val waitingObject = new Object
	private val lock = new Object
	private var buffer : BzData = null

	def time = (new Date).getTime

	def start(queue : BzrcQueue) {
		executor.scheduleWithFixedDelay(new Runnable {
			def run() {
				val start = time
				val tmp = queue.invokeAndWait(_.data)
				lock synchronized  {
					buffer = tmp
				}
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
		}, 0, 100, TimeUnit.MILLISECONDS)
	}

	def data = {
		var tmp : BzData = null
		lock synchronized  {
			tmp = buffer
		}
		tmp
	}

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
	private val locker = new ReentrantLock
	def findData(data : BzData) : Seq[F]
	def buffer = findData(RefreshableData.data)

	lazy val availableData = lock {
		val data = buffer.map(convert(_))
		loaded(data)
		data
	}

	protected def loaded(data : Traversable[T]) {}

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
}
