package cs470.agents

import cs470.utils._
import cs470.domain.Constants._
import cs470.domain.Point
import cs470.bzrc.{Tank, DataStore}
import cs470.movement.{PotentialFieldGenerator, SearchPath, PotentialFieldsMover}

class MultiAgent(host: String, port: Int) extends Agent(host, port) with Threading {

  import MultiAgent._

  def run() {
    //		actor {
    //			new SniperAgent(store.tanks(0), store).apply()
    //		}
    actor {
      new DecoyAgent(store.tanks(0), store).apply()
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

  val opponentFlag = bases.find(_.color == "green").get.points.center
  val mytank = tank

  val shotrange: Int = 200
  //constants("shotrange")

  val prePositionPoint: Point

  def gotoPrePosition() {
    LOG.info("Moving " + tank.callsign + " to preposition")
    val mover = {
      val toSafePoint = new SearchPath(store, prePositionPoint, tank.id, "prePosition_" + tank.tankId, "prePosition_" + tank.tankId)
      new cs470.visualization.PFVisualizer(toSafePoint, "toPrePosition_" + tank.id + ".gpi", obstacles, constants("worldsize"), 25)

      new PotentialFieldsMover(store) {
        def path = toSafePoint.getPathVector(mytank.location)
        val goal = prePositionPoint
        val tank = mytank
        override val moveWhileTurning = true
        override val howClose = 30
      }
    }

    mover.moveAlongPotentialField()

    LOG.info("Preposition was achieved by " + tank.callsign)
  }

 def returnHome() {
    LOG.info("Sending " + tank.callsign + " home")
    val goalFlag = bases.find(_.color == constants("team")).get.points.center
    val mover = {
      val toHomeBase = new SearchPath(store, goalFlag, tank.id, "returnHome_" + tank.tankId, "returnHome_" + tank.tankId)
      new cs470.visualization.PFVisualizer(toHomeBase, "toHomeBase.gpi", obstacles, constants("worldsize"), 25)

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

import cs470.domain.Vector

//class SniperAgent(tank: Tank, store: DataStore) extends MultiAgentBase(tank, store) {
//  val prePositionPoint = opponentFlag - new Point(shotrange, shotrange)
//
//  override def apply() {
//    super.apply()
//
//  }
//}

class DecoyAgent(tank: Tank, store: DataStore) extends MultiAgentBase(tank, store) {
  val prePositionPoint = opponentFlag - new Point(shotrange + 10, 0)
  override val LOG = org.apache.log4j.Logger.getLogger(classOf[DecoyAgent])

  def alternate(dir: String, direction: Int) {
    val target = prePositionPoint + new Point(0, direction * 130)

    LOG.info("Moving decoy (" + tank.callsign + ") " + dir + " to " + target)

    val searcher = new PotentialFieldGenerator(store) {
      def getPathVector(point: Point) = new Vector(target - point)
    }
    new cs470.visualization.PFVisualizer(searcher, "go_" + dir + ".gpi", obstacles, constants("worldsize"), 25)

    new PotentialFieldsMover(store) {
      val tank = mytank
      val goal = target
      override val moveWhileTurning = true
      override val howClose = 30
      def path = searcher.getPathVector(tank.location)
    }.moveAlongPotentialField()
  }

  def north() {
    alternate("north", 1)
  }

  def south() {
    alternate("south", -1)
  }

  override def apply() {
    LOG.info("Starting decoy")
    super.apply()

    while (true) {
      north()
      south()
    }
  }
}

object MultiAgent extends AgentCreator {
  val LOG = org.apache.log4j.Logger.getLogger("cs470.agents.MultiAgent")

  def name = "multi"

  def create(host: String, port: Int) = new MultiAgent(host, port)
}
