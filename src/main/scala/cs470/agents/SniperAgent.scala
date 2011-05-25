package cs470.agents

import cs470.domain.Constants._
import cs470.domain.Point
import cs470.domain.Vector
import cs470.utils._
import Angle._
import cs470.bzrc._
import cs470.movement.{SearchPath, PotentialFieldsMover, PotentialFieldGenerator, pfFindFlag}

class SniperAgent(tank: Tank, store: DataStore, decoy : DecoyAgent) extends MultiAgentBase(tank, store) with Threading {
  val offset = 50
  val prePositionPoint = opponentFlag - new Point(shotrange - offset, 0)
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
    val target = tank.location + new Point(offset + 5, 0)

    LOG.info("Moving sniper (" + tank.callsign + ") to sniper position")

    val searcher = new PotentialFieldGenerator(store) {
      def getPathVector(point: Point) = new Vector(AttractivePF(point, target, 5, 10, 1))
    }

    if (LOG.isDebugEnabled)
      new cs470.visualization.PFVisualizer(searcher, "gotoSniperPosition.gpi", obstacles, constants("worldsize"), 25)

    new PotentialFieldsMover(store) {
      val tank = mytank
      val goal = target
      override val moveWhileTurning = true
      override val howClose = 30

      def path = searcher.getPathVector(tank.location)
    }.moveAlongPotentialField()
  }

  import cs470.domain.Vector

  def killEnemy(enemy: Enemy) {
    def vector = new Vector(enemy.location - tank.location)
    while (enemy.status == "alive") {
      //println("Trying to kill " + enemy.callsign + " - angle was " + angle.degree)
      val angle = vector.angle
      tank.moveToAngle(angle)
      LOG.debug("shooting [" + enemy.callsign + "]")
      tank.shoot()
      RefreshableData.waitForNewData()
    }
  }

  val Kp = 1
  val Kd = 4.5
  val tol = degree(2).radian
  val tolv = .1
  val maxVel: Double = constants("tankangvel")
  val worldsize: Int = constants("worldsize")
  val offsetVector = new Vector(new Point(worldsize / 2, worldsize / 2))
  val maxMagnitude = 100.0
  val maxVelocity = 1
  val team = constants("team")
  val turningSpeed = 0.6

  import java.lang.Math._

  def gotoFlag() {
    LOG.info("Moving sniper [" + tank.callsign + "] to get " + goalFlag.color + " flag")
    val mover = {

      val toFlag = new PotentialFieldGenerator(store) {
        def getPathVector(point: Point) = new Vector(AttractivePF(point, opponentFlag, 5, 10, 1))
      }

      if (LOG.isDebugEnabled)
        new cs470.visualization.PFVisualizer(toFlag, "toFlag.gpi", obstacles, constants("worldsize"), 25)

      new PotentialFieldsMover(store) {
        def path = toFlag.getPathVector(mytank.location)

        val goal = opponentFlag
        val tank = mytank
        override val moveWhileTurning = true
        override val howClose = 30
      }
    }

    mover.moveAlongPotentialField()

    LOG.info("Sniper [" + tank.callsign + "] captured the " + goalFlag.color + " flag")
  }
}

