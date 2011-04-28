package cs470.agents

import cs470.BzrcQueue

trait Agent {
  final def start(host : String, port : Int) {
	  run(new BzrcQueue(host, port))
  }

  def run(queue : BzrcQueue)
}