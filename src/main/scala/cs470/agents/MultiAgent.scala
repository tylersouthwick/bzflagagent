package cs470.agents

import cs470.utils._
import cs470.movement.{SearchPath, PotentialFieldsMover}

class MultiAgent(host: String, port: Int) extends Agent(host, port) with Threading {

  import MultiAgent._


  def run() {

    LOG.info("Running multiagent")

    val mytank = store.tanks(0)
    val mover = new PotentialFieldsMover(store) {
      val path = new SearchPath(store).getPathVector(mytank.location)
      val tank = mytank
    }

    loop {
      mover.moveAlongPotentialField()
    }
  }
}


object MultiAgent extends AgentCreator {
  val LOG = org.apache.log4j.Logger.getLogger("cs470.agents.MultiAgent")

  def name = "multi"

  def create(host: String, port: Int) = new MultiAgent(host, port)
}
