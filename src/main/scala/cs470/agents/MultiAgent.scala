package cs470.agents

import cs470.bzrc.Tank
import cs470.utils._
import cs470.visualization.SearchVisualizer
import java.io.{PrintWriter, BufferedOutputStream, FileOutputStream}
import cs470.domain.Constants._
import cs470.movement.search.{A_StarSearcher, Searcher}
import cs470.domain.{Point, UsableOccgrid}

class MultiAgent(host: String, port: Int) extends Agent(host, port) with Threading {

  import MultiAgent._

  trait AgentSearcher extends A_StarSearcher {
    val worldSize : Int = constants("worldsize")
    val tankRadius : Double = constants("tankradius")

    val datastore = store
    val start = store.tanks(tankId).location
    val q = queue
    lazy val occgrid = new UsableOccgrid(100,obstacles,tankRadius,worldSize,enemies)

  }

  def run() {

    LOG.info("Running multiagent")

    val safePoint = flags.find(_.color == "green").get.location - new Point(15,15)

    val searcher = new AgentSearcher {
      val name = "aStarSafePoint"
      val tankId = 0
      val goal = safePoint
      val title = "To Safe point"
      val filename = "Safe point searcher"
    }

    searcher.search()

    System.exit(0)
  }
}



object MultiAgent extends AgentCreator {
  val LOG = org.apache.log4j.Logger.getLogger("cs470.agents.MultiAgent")

  def name = "multi"

  def create(host: String, port: Int) = new MultiAgent(host, port)
}