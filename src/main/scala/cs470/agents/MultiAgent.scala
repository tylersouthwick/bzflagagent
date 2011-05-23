package cs470.agents

import cs470.utils._
import cs470.movement.{TankPathFinder, SearchPath, PotentialFieldsMover}
import cs470.visualization.PFVisualizer
import cs470.domain.Constants._

class MultiAgent(host: String, port: Int) extends Agent(host, port) with Threading {

  import MultiAgent._


  def run() {

    LOG.info("Running multiagent")

    val mytank = store.tanks(0)
    val searcher = new SearchPath(store)
    val worldsize: Int = constants("worldsize")

    val visualizer = new PFVisualizer(searcher, "vis.gpi", obstacles, worldsize, 25)

    loop {

      val mover = new PotentialFieldsMover(store) {
        val finder = new TankPathFinder {
          val color = mytank.callsign

          def path = searcher.getPathVector(mytank.location)
        }
        val tank = mytank
      }

      mover.moveAlongPotentialField()
    }
  }
}


object MultiAgent extends AgentCreator {
  val LOG = org.apache.log4j.Logger.getLogger("cs470.agents.MultiAgent")

  def name = "multi"

  def create(host: String, port: Int) = new MultiAgent(host, port)
}