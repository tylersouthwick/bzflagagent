package cs470.utils

import java.util.concurrent.Executors

object Threading {
	val pool = Executors.newCachedThreadPool
}

trait Threading {

	def timeout(milliseconds: Long)(callback: => Unit) {
		sleep(milliseconds)
		callback
	}

	/*
  def reactTimeout(milliseconds: Long)(callback: => Unit) {
    reactWithin(milliseconds) {
      case TIMEOUT => callback
    }
  }

  def receiveTimeout(milliseconds: Long)(callback: => Unit) {
    receiveWithin(milliseconds) {
      case TIMEOUT => callback
    }
  }
  */

  def sleep(milliseconds: Long) {
	  Thread.sleep(milliseconds)
	  //receiveTimeout(milliseconds) {}
  }

	def actor(callback : => Unit) {
		Threading.pool.submit(new Runnable {
			def run() {
				try {
					callback
				} catch {
					case t : Throwable => {
						t.printStackTrace()
					}
				}
			}
		})
	}

	def loop(callback : => Unit) {
		while (true) callback
	}
}


