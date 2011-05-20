package cs470.agents

import cs470.bzrc.Tank
import cs470.utils._
import cs470.visualization.SearchVisualizer
import java.io.{PrintWriter, BufferedOutputStream, FileOutputStream}
import cs470.domain.UsableOccgrid
import cs470.domain.Constants._

class MultiAgent(host: String, port: Int) extends Agent(host, port) with Threading {

  import MultiAgent._

  def run() {
    LOG.info("Running multiagent")

    val worldsize : Int = constants("worldsize")
    val tankradius : Double = constants("tankradius")

    val occgrid = new UsableOccgrid(100,obstacles,tankradius,worldsize,enemies)
    val file = new PrintWriter(new BufferedOutputStream(new FileOutputStream("multi.out.gpi")))

    file.println(occgrid.toString())
    file.println(occgrid.print)

    file.close()

    System.exit(0)
  }
}

object MultiAgent extends AgentCreator {
  val LOG = org.apache.log4j.Logger.getLogger("cs470.agents.MultiAgent")

  def name = "multi"

  def create(host: String, port: Int) = new MultiAgent(host, port)
}