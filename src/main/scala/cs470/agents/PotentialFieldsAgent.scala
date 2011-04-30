package cs470.agents

import scala.actors._
import Actor._
import cs470.bzrc.Tank
import cs470.utils.{Threading, Units}

class PotentialFieldAgent(host: String, port: Int) extends Agent(host, port) with Threading with Units {

  import PotentialFieldAgent._

	def run() {
		LOG.info("Running potential field agent")
		val tanks = myTanks
    moveAlongPotentialField(tanks.apply(1))
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
