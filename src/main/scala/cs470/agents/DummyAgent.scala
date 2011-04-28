package cs470.agents

import cs470.domain.MyTank
import scala.actors._
import Actor._

class DummyAgent(host: String, port: Int) extends Agent(host, port) {
  import DummyAgent._

  def run {
    LOG.info("Running dummy agent")
    val tanks = queue.invokeAndWait(_.mytanks)
    tanks.foreach(moveDummyTanks(_))
  }

  def moveDummyTanks(tank: MyTank) = {
    LOG.info("Starting tank #" + tank.id + " on dummy path")

    //Go forward a bit, then rotate 60 degrees
    timeout(3000) {
        LOG.debug("Tank #" + tank.id + " is moving")
        tank.speed(0.0f)
        tank.speed(1.0f)
        //wait 3-8 seconds
        //rotate ~60 degrees
    }

    //Shoot every 1.5-2.5 seconds
    timeout(2000) {
      LOG.debug("Tank #" + tank.id + " is shooting")
      tank.shoot
    }
  }
}

object DummyAgent extends AgentCreator {
  val LOG = org.apache.log4j.Logger.getLogger("cs470.agents.DummyAgent")
	def name = "dummy"
	def create(host : String, port : Int) = new DummyAgent(host, port)
}
