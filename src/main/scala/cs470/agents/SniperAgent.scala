package cs470.agents

import cs470.domain.Constants._
import cs470.domain.Point
import cs470.domain.Vector
import cs470.utils._
import Angle._
import cs470.bzrc._
import cs470.movement.{SearchPath, PotentialFieldsMover, PotentialFieldGenerator, pfFindFlag}
import cs470.visualization.PFVisualizer

class SniperAgent(tank: Tank, store: DataStore, decoy: DecoyAgent) extends MultiAgentBase(tank, store) with Threading {
  val offset = 50
  val prePositionPoint = new Point(45, -250)
  //opponentFlag - new Point(shotrange + offset, 0)
  val sniperPosition = new Point(180, -230)
  override val LOG = org.apache.log4j.Logger.getLogger(classOf[SniperAgent])

  def enemies = store.enemies.filter(_.color == "green").filter(_.status == "alive")

  override def apply() {
    loop {
      super.apply()

      //Wait for decoy to get into position
      decoy.waitUntilReady()

      gotoSniperPosition()

      tank.speed(0)

      enemies.foreach {
        enemy =>
          LOG.info("Sniper [" + tank.callsign + "] is aiming for " + enemy.callsign)
          killEnemy(enemy)
          LOG.info("Sniper [" + tank.callsign + "] has killed " + enemy.callsign)
      }


      gotoFlag()

      returnHome()

    }
  }

  def gotoSniperPosition() {
    LOG.info("Moving sniper (" + tank.callsign + ") to sniper position")

    val searcher = new PotentialFieldGenerator(store) {
      def getPathVector(point: Point) = new Vector(AttractivePF(point, sniperPosition, 5, 10, 1))
    }

    if (LOG.isDebugEnabled)
      new PFVisualizer {
        val samples = 25
        val pathFinder = searcher
        val plotTitle = "Sniper moves into position"
        val fileName = "gotoSniperPosition"
        val name = "gotoSniperPosition"
        val worldsize: Int = constants("worldsize")
        val obstacleList = obstacles
      }.draw()
    //      new cs470.visualization.PFVisualizer(searcher, "gotoSniperPosition.gpi", obstacles, constants("worldsize"), 25)

    new PotentialFieldsMover(store) {
      val tank = mytank
      val goal = sniperPosition
      override val moveWhileTurning = true
      override val howClose = 30

      def path = searcher.getPathVector(tank.location)
    }.moveAlongPotentialField()
  }

  def killEnemy(enemy: Enemy) {
    def vector = new Vector(enemy.location - tank.location)
    while (enemy.status == "alive") {
      val angle = vector.angle
      println("Trying to kill " + enemy.callsign + " - angle was " + angle.degree)
      tank.moveToAngle(angle)
      println("shooting [" + enemy.callsign + "]")
      tank.shoot()
      RefreshableData.waitForNewData()
    }
  }

  private def doGotoFlag() {
    val mover = {

      val toFlag = new PotentialFieldGenerator(store) {
        def getPathVector(point: Point) = new Vector(AttractivePF(point, opponentFlag, 5, 10, 1))
      }

      if (LOG.isDebugEnabled)
        new PFVisualizer {
          val samples = 25
          val pathFinder = toFlag
          val plotTitle = "Sniper to Flag"
          val fileName = "toFlag"
          val name = "sniper_to_flag"
          val worldsize: Int = constants("worldsize")
          val obstacleList = obstacles
        }.draw()
      //        new cs470.visualization.PFVisualizer(toFlag, "toFlag.gpi", obstacles, constants("worldsize"), 25)

      new PotentialFieldsMover(store) {
        def path = toFlag.getPathVector(mytank.location)

        val goal = opponentFlag
        val tank = mytank
        //override val moveWhileTurning = true
        override def inRange(vector: Vector) = {
          //println("flag: " + tank.flag + "->" + tank.flag.isEmpty)
          tank.flag.isEmpty
        }
      }
    }

    mover.moveAlongPotentialField()
  }

  def gotoFlag() {
    LOG.info("Moving sniper [" + tank.callsign + "] to get " + goalFlag.color + " flag")

    while (tank.flag.isEmpty) doGotoFlag()

    LOG.info("Sniper [" + tank.callsign + "] captured the " + goalFlag.color + " flag")
  }
}

