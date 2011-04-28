package cs470.agents

class DummyAgent(host : String,port : Int) extends Agent(host,port) {
  def run {
      val tanks = queue.invokeAndWait(_.mytanks)
      tanks.foreach(tank => tank.speed(1))
  }
}

object DummyAgent extends AgentCreator {
	def name = "dummy"
	def create(host : String, port : Int) = new DummyAgent(host, port)
}
