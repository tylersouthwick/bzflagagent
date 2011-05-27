package cs470.agents

import cs470.utils.Threading

class ScoutAgent(host:String, port:Int) extends Agent(host,port) with Threading {
  import ScoutAgent._

  def run() {
    LOG.info("Starting scout agent")



    LOG.info("Scout agent done")
    System.exit(0)
  }
}

object ScoutAgent extends AgentCreator {
  val LOG = org.apache.log4j.Logger.getLogger(classOf[ScoutAgent])

  def name = "scout"

  def create(host: String, port: Int) = new ScoutAgent(host, port)
}