package cs470

import scala.actors._
import java.util.concurrent.{Callable, Executors}

class BzrcQueue(host: String, port: Int) {

	private val LOG = org.apache.log4j.Logger.getLogger(classOf[BzrcQueue])

	private val con = new BzFlagConnection(host, port)
	private val queue = Executors.newSingleThreadExecutor

	def invoke(callback: BzFlagConnection => Unit) {
		queue.execute(new Runnable () {
			def run() {
				callback(con)
			}
		})
	}

	def invokeAndWait[T](callback: BzFlagConnection => T): T = {
		queue.submit(new Callable[T] {
			def call() = callback(con)
		}).get
	}

}
