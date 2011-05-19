package cs470.agents

import cs470.bzrc.Tank
import cs470.utils._

class MultiAgent(host: String, port: Int) extends Agent(host, port) with Threading {

  import MultiAgent._

  def run() {
    LOG.info("Running multiagent")
  }

}

object MultiAgent extends AgentCreator {
  val LOG = org.apache.log4j.Logger.getLogger("cs470.agents.MultiAgent")

  def name = "multi"

  def create(host: String, port: Int) = new MultiAgent(host, port)
}