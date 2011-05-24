package cs470.agents

import cs470.utils._
import cs470.domain.Constants._
import cs470.movement.{SearchPath, PotentialFieldsMover}
import javax.management.remote.rmi._RMIConnection_Stub

class MultiAgent(host: String, port: Int) extends Agent(host, port) with Threading {

  import MultiAgent._

  def run() {

    LOG.info("Running multiagent")

    val mytank = store.tanks(0)
	  val tank = mytank

	val mover = {
	  val searchPath = new SearchPath(store)
	  new cs470.visualization.PFVisualizer(searchPath, "searchPath.gpi", obstacles, constants("worldsize"), 50)

	  new PotentialFieldsMover(store) {
		  def path = searchPath.getPathVector(mytank.location)
		  val tank = mytank
	  }
	}
	  val maxDistance = 30.0
	  def inRange = !store.enemies.filter(_.location.distance(tank.location) < maxDistance).isEmpty
    while (!inRange) {
      		mover.moveAlongPotentialField(inRange)
    }

	  tank.setAngularVelocity(0)
	  tank.speed(0)
	  println("Stopping agent")
  }
}


object MultiAgent extends AgentCreator {
  val LOG = org.apache.log4j.Logger.getLogger("cs470.agents.MultiAgent")

  def name = "multi"

  def create(host: String, port: Int) = new MultiAgent(host, port)
}
