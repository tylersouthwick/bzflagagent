package cs470.agents

class DummyAgent(host : String,port : Int) extends Agent(host,port) {
  def run {
      val tanks = queue.invokeAndWait(_.mytanks)
      tanks.foreach(tank => tank.speed(1))
  }
}