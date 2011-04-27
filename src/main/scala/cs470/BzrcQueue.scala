package cs470

import scala.actors._
import Actor._

class BzrcQueue(host: String, port: Int) {

	private val LOG = org.apache.log4j.Logger.getLogger(classOf[BzrcQueue])

	private case class ItemAvailable()

	private val con = new BzFlagConnection(host, port)
	private val callbacks = new java.util.concurrent.ConcurrentLinkedQueue[BzFlagConnection => Unit]


	def invoke(callback: BzFlagConnection => Unit) {
		callbacks.add(callback)
		LOG.debug("sending ItemAvailable")
		queue ! ItemAvailable
	}

	def invokeAndWait[T](callback: BzFlagConnection => T): T = {
		val me = self
		callbacks.add {
			con =>
				me ! callback(con)
		}

		queue ! ItemAvailable
		receive {
			case t: AnyRef => t.asInstanceOf[T]
			case _ => throw new IllegalStateException
		}
	}

	private val queue = actor {
		loop {
			react {
				case ItemAvailable => {
					LOG.debug("got ItemAvailable")
					val callback = callbacks.poll
					callback(con)
				}
			}
		}
	}

}
