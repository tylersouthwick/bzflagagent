package cs470.agents

import cs470.utils._
import cs470.domain.Constants._
import cs470.domain.Point
import cs470.bzrc.{Tank, DataStore}
import cs470.movement.{PotentialFieldGenerator, SearchPath, PotentialFieldsMover}
import cs470.visualization.PFVisualizer

import cs470.domain.Vector

class DecoyAgent(tank: Tank, store: DataStore) extends MultiAgentBase(tank, store) {
  val ready = new java.util.concurrent.Semaphore(0)
  val prePositionPoint = new Point(80, 0)
  override val LOG = org.apache.log4j.Logger.getLogger(classOf[DecoyAgent])

  def alternate(dir: String, direction: Int) {
    val target = new Point(110, direction * 200)

    LOG.info("Moving decoy (" + tank.callsign + ") " + dir + " to " + target)

    val searcher = new PotentialFieldGenerator(store) {
      def getPathVector(point: Point) = new Vector(AttractivePF(point, target, 5, 10, 20))

    }

    if (LOG.isDebugEnabled)
      new PFVisualizer {
        val samples = 25
        val pathFinder = searcher
        val plotTitle = "Decoy PF " + dir
        val fileName = "decoy_go_" + dir
        val name = "decoy_" + dir
        val worldsize: Int = constants("worldsize")
        val obstacleList = obstacles
      }.draw()
    //      new cs470.visualization.PFVisualizer(searcher, "go_" + dir, obstacles, constants("worldsize"), 25) {
    //        override val saveType = "eps"
    //        override val title = ""
    //      }

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

    ready.release()

    while (true) {
      north()
      south()
    }
  }

  def waitUntilReady() {
    ready.acquire()
  }
}


