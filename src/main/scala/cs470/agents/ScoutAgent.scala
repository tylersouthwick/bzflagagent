package cs470.agents

import cs470.utils.Threading
import cs470.domain.BayesianOccgrid
import cs470.visualizer.BayesianVisualizer

class ScoutAgent(host:String, port:Int) extends Agent(host,port) with Threading {
  import ScoutAgent._

  def run() {
    LOG.info("Starting scout agent")

    val tank = myTanks(0)
	  val occgrid = new BayesianOccgrid with BayesianVisualizer {
		  val constants = store.constants
	  }
	occgrid.startVisualizer()
    occgrid.update(tank)

    LOG.info("Scout agent done")
  }
}

object ScoutAgent extends AgentCreator {
  val LOG = org.apache.log4j.Logger.getLogger(classOf[ScoutAgent])

  def name = "scout"

  def create(host: String, port: Int) = new ScoutAgent(host, port)
}