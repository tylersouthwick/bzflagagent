package cs470.utils

import actors._
import Actor._

trait Threading {

  def timeout(milliseconds: Long)(callback: => Unit) = receiveTimeout(milliseconds)(callback)

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

  def sleep(milliseconds: Long) {
	  receiveTimeout(milliseconds) {}
  }
}


