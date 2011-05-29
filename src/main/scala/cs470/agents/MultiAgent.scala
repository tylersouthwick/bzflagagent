package cs470.agents

import cs470.utils._
import cs470.domain.Constants._
import cs470.domain.Point
import cs470.bzrc.{Tank, DataStore}
import cs470.movement.{SearchPath, PotentialFieldsMover}
import cs470.visualization.PFVisualizer

class MultiAgent(host: String, port: Int) extends Agent(host, port) with Threading {

  import MultiAgent._

  def run() {
    val tanks = store.tanks
    if (tanks.size != 2) {
      LOG.error("Invalid number of tanks.  There must be 2 tanks")
      System.exit(-1)
    }

    val decoy = new DecoyAgent(tanks(0), store)
    val sniper = new SniperAgent(tanks(1), store, decoy)

    actor {
      decoy()
    }
    actor {
      sniper()
    }

    LOG.info("running multi agent")
  }
}

abstract class MultiAgentBase(tank: Tank, store: DataStore) {
  val LOG = org.apache.log4j.Logger.getLogger(classOf[MultiAgentBase])

  val flags = store.flags
  val constants = store.constants
  val obstacles = store.obstacles
  val bases = store.bases

  val goalFlag = flags.find(_.color == "green").get
  val opponentFlag = goalFlag.location
  val mytank = tank

  val shotrange: Int = constants("shotrange")

  val prePositionPoint: Point

  def gotoPrePosition() {
    LOG.info("Moving " + tank.callsign + " to preposition")
    val mover = {
      val toSafePoint = new SearchPath(store) {
	      val tankIdd = tank.tankId
	      override val searchName = "prePositionPath_" + tank.tankId
	      override val searchTitle = "Preposition path for " + tank.tankId
	      val searchGoal = prePositionPoint
      }

      if (LOG.isDebugEnabled)
        new PFVisualizer {
          val samples = 25
          val pathFinder = toSafePoint
          val plotTitle = tank.callsign + " to safe point"
          val fileName = tank.callsign + "_safePoint"
          val name = tank.callsign + "SafePoint"
          val worldsize: Int = constants("worldsize")
          val obstacleList = obstacles
        }.draw()
      //        new cs470.visualization.PFVisualizer(toSafePoint, "PrePositionPF_" + tank.id, obstacles, constants("worldsize"), 25) {
      //          override val saveType = "eps"
      //          override val title = ""
      //        }

      new PotentialFieldsMover(store) {
        def path = toSafePoint.getPathVector(mytank.location)

        val goal = prePositionPoint
        val tank = mytank
        override val moveWhileTurning = true
        override val howClose = 30
      }
    }

    mover.moveAlongPotentialField()

    LOG.info("PrePosition was achieved by " + tank.callsign)
  }

  def returnHome() {
    LOG.info("Sending " + tank.callsign + " home")
    val goalFlag = bases.find(_.color == constants("team")).get.points.center
    val mover = {
      val toHomeBase = new SearchPath(store){
	      val tankIdd = tank.tankId
	      override val searchName = "returnHome_" + tank.tankId
	      override val searchTitle = "Path home for " + tank.tankId
	      val searchGoal = goalFlag
      }
      if (LOG.isDebugEnabled)
        new PFVisualizer {
          val samples = 25
          val pathFinder = toHomeBase
          val plotTitle = "To Home Base"
          val fileName = "toHomeBase"
          val name = "toHomeBase"
          val worldsize: Int = constants("worldsize")
          val obstacleList = obstacles
        }.draw()
      //        new cs470.visualization.PFVisualizer(toHomeBase, "toHomeBase.gpi", obstacles, constants("worldsize"), 25)

      new PotentialFieldsMover(store) {
        def path = toHomeBase.getPathVector(mytank.location)

        val goal = goalFlag
        val tank = mytank
        override val moveWhileTurning = true
      }
    }

    mover.moveAlongPotentialField()
  }

  def apply() {
    //find current state
    gotoPrePosition()
  }
}

object MultiAgent extends AgentCreator {
  val LOG = org.apache.log4j.Logger.getLogger("cs470.agents.MultiAgent")

  def name = "multi"

  def create(host: String, port: Int) = new MultiAgent(host, port)
}
