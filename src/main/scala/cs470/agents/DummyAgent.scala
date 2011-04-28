package cs470.agents

import cs470.domain.MyTank
import scala.actors._
import Actor._

class DummyAgent(host: String, port: Int) extends Agent(host, port) {
  def run {
    val tanks = queue.invokeAndWait(_.mytanks)
    tanks.foreach(moveDummyTanks(_))
  }

  def moveDummyTanks(tank: MyTank) = {
    //Go forward a bit, then rotate 60 degrees
    timeout(3000) {
        tank.speed(1.0f)
        //wait 3-8 seconds
        tank.speed(0.0f)
        //rotate ~60 degrees
    }

    //Shoot every 1.5-2.5 seconds
    timeout(2000) {
      tank.shoot
    }
  }

}

object DummyAgent extends AgentCreator {
	def name = "dummy"
	def create(host : String, port : Int) = new DummyAgent(host, port)
}
