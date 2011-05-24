package cs470.agents

import cs470.utils._
import cs470.domain.Constants._
import cs470.domain.Point
import cs470.bzrc.{Tank, DataStore}
import cs470.movement.{PotentialFieldGenerator, SearchPath, PotentialFieldsMover}

import cs470.domain.Vector

class DecoyAgent(tank: Tank, store: DataStore) extends MultiAgentBase(tank, store) {
  val prePositionPoint = opponentFlag - new Point(shotrange + 50, 0)
  override val LOG = org.apache.log4j.Logger.getLogger(classOf[DecoyAgent])

  def alternate(dir: String, direction: Int) {
    val target = prePositionPoint + new Point(25, direction * 100)

    LOG.info("Moving decoy (" + tank.callsign + ") " + dir + " to " + target)

    val searcher = new PotentialFieldGenerator(store) {
      def getPathVector(point: Point) = new Vector(target - point)
    }

	if (LOG.isDebugEnabled)
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


