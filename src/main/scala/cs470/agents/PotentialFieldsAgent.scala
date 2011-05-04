package cs470.agents

import cs470.bzrc.Tank
import cs470.utils.Threading
import cs470.visualization.Visualizer
import cs470.domain.Constants._
import cs470.movement.{pfReturnToGoal, pfFindFlag}

class PotentialFieldAgent(host: String, port: Int) extends Agent(host, port) with Threading {

  import PotentialFieldAgent._

	def run() {
		LOG.info("Running potential field agent")
		val tanks = myTanks
		//pick a tank
		val tank = tanks(1)
		moveAlongPotentialField(tank)

		val pfgen = new pfReturnToGoal(queue,"blue")
		val vis = new Visualizer(pfgen,"pf.gpi",obstacles, convertInt(constants("worldsize")),25)
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
