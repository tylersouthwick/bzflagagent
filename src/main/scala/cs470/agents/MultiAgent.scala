package cs470.agents

import cs470.utils._
import cs470.domain.Constants._
import cs470.movement.{SearchPath, PotentialFieldsMover}

class MultiAgent(host: String, port: Int) extends Agent(host, port) with Threading {

  import MultiAgent._

  def run() {

    LOG.info("Running multiagent")
    val opponentFlag = flags.find(_.color == "green").get.location

    {
      val mytank = store.tanks(0)
      val tank = mytank

      val mover = {
        val toSafePoint = new SearchPath(store, opponentFlag, tank.id)
        new cs470.visualization.PFVisualizer(toSafePoint, "toSafePoint.gpi", obstacles, constants("worldsize"), 25)

        new PotentialFieldsMover(store) {
          def path = toSafePoint.getPathVector(mytank.location)

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
      LOG.info("Stopping agent: " + tank.callsign)
    }

    {
      val mytank = store.tanks(0)
      val tank = mytank
      val goalFlag = bases.find(_.color == constants("team")).get.points.center

      val mover = {
        val toHomeBase = new SearchPath(store, goalFlag, tank.id)
        new cs470.visualization.PFVisualizer(toHomeBase, "toHomeBase.gpi", obstacles, constants("worldsize"), 25)

        new PotentialFieldsMover(store) {
          def path = toHomeBase.getPathVector(mytank.location)

          val tank = mytank
        }
      }


      loop {
        mover.moveAlongPotentialField(false)
      }
    }
  }
}

object MultiAgent extends AgentCreator {
  val LOG = org.apache.log4j.Logger.getLogger("cs470.agents.MultiAgent")

  def name = "multi"

  def create(host: String, port: Int) = new MultiAgent(host, port)
}
