package cs470.agents

import cs470.bzrc.Tank
import cs470.utils.Threading
import cs470.visualization.PFVisualizer
import cs470.movement.PotentialFieldGenerator
import cs470.domain.Constants._

class PotentialFieldAgent(host: String, port: Int) extends Agent(host, port) with Threading {

  import PotentialFieldAgent._

	def run() {
		LOG.info("Running potential field agent")
		val tanks = myTanks
    moveAlongPotentialField(tanks.apply(1))

    val pfgen = new PotentialFieldGenerator(queue)
    val vis = new PFVisualizer(pfgen,"pf.gpi",convertInt(constants("worldsize")),25)

	}

  def moveAlongPotentialField(tank : Tank) {
    tank.speed(1)
  }

}

object PotentialFieldAgent extends AgentCreator {
  val LOG = org.apache.log4j.Logger.getLogger("cs470.agents.PotentialFieldAgent")

  def name = "pf"

  def create(host: String, port: Int) = new PotentialFieldAgent(host, port)
}
