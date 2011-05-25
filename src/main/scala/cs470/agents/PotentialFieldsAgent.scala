package cs470.agents

import cs470.domain._
import Constants._
import cs470.movement._
import cs470.utils._
import Angle._
import java.lang.Math._
import cs470.bzrc._
import cs470.visualization.PFVisualizer

class PotentialFieldAgent(host: String, port: Int) extends Agent(host, port) with Threading {

  import PotentialFieldAgent._

  def run() {
    LOG.info("Running potential field agent")
    val tanks = myTanks
    //pick a tank
    tanks.foreach(moveAlongPotentialField)

    /*
     val pfgen = new pfReturnToGoal(store,"blue")
     val vis = new Visualizer(pfgen,"pf.gpi",obstacles, convertInt(constants("worldsize")),25)
     */
  }

  val Kp = 1
  val Kd = 4.5
  val tol = degree(2).radian
  val tolv = .1
  val maxVel: Double = constants("tankangvel")
  val worldSize: Int = constants("worldsize")
  val offsetVector = new Vector(new Point(worldSize / 2, worldSize / 2))
  val maxMagnitude = 100.0
  val maxVelocity = 1
  val team = constants("team")
  val turningSpeed = 0.6

  trait TankPathFinder {
    def path: Vector

    val color: String
  }

  def moveAlongPotentialField(tank: Tank) {
    val flagFinders = flags filter (_.color != team) map {
      flag =>
        LOG.debug("flag: " + flag.color)
        new TankPathFinder {
          private val findFlag = new pfFindFlag(store, flag.color)

          def path = {
            if (LOG.isDebugEnabled)
              new PFVisualizer {
                val samples = 25
                val pathFinder = findFlag
                val plotTitle = "PF to " + flag.color
                val fileName = "pf_" + flag.color
                val name = "pf_" + flag.color
                val worldsize = worldSize
                val obstacleList = obstacles
              }.draw()
            //							new PFVisualizer(findFlag, "pf" + flag.color + ".gpi", obstacles, worldsize, 25)
            findFlag.getPathVector(tank.location)
          }

          val color = flag.color

          def possessingTeamColor = flag.possessingTeamColor

          def location = flag.location
        }
    }
    val homeFinder = new {
      val finder = new pfReturnToGoal(store, team)

      def path = finder.getPathVector(tank.location)
    }

    actor {
      loop {
        def waitForNewData() {
          RefreshableData.waitForNewData()
          tank.shoot()
        }
        tank.shoot();

        def move(pdVector: => Vector) {
          //	tank.speed(vector.magnitude / maxMagnitude)
          //val (angle, time) = tank.moveAngle(vector.angle)

          def pdController(error0: Radian, vector: Vector) {
            val targetAngle = vector.angle
            val angle = tank.angle
            LOG.debug("targetAngle: " + targetAngle.degree)
            LOG.debug("angle: " + angle.degree)
            val error = targetAngle - angle

            LOG.debug("error: " + error.degree)
            val rv = (Kp * error + Kd * (error - error0) / 200);
            LOG.debug("rv: " + rv)
            val v = if (rv > maxVel) 1 else rv / maxVel
            LOG.debug("v: " + v)

            if (abs(error) < tol && abs(v) < tolv) {
              LOG.debug("Done Turning");
              tank.setAngularVelocity(0f)
              val speed = {
                val m = vector.magnitude
                LOG.debug("magnitude: " + m)
                val result = m / 30.0
                if (result > maxVelocity) {
                  maxVelocity
                } else {
                  result
                }
              }
              LOG.debug("setting speed: " + speed)
              tank.speed(speed)
              waitForNewData()
            } else {
              //Agents.LOG.debug("Setting velocity to " + v)
              tank.setAngularVelocity(v)
              //slow it down to turn
              LOG.debug("setting speed: " + turningSpeed)
              tank.speed(turningSpeed)
              waitForNewData()
              pdController(error, pdVector)
            }
          }

          pdController(radian(0), pdVector)
        }
        def findFlag() {
          val location = tank.location
          val finder = flagFinders.foldLeft((Double.MaxValue, null: TankPathFinder)) {
            (closest, finder) =>
              if (finder.possessingTeamColor == team)
                closest
              else {
                val magnitude = finder.location.distance(location)
                if (magnitude < closest._1)
                  (magnitude, finder)
                else
                  closest
              }
          }._2

          if (finder == null) {
            LOG.debug("my team has all the flags, so go home")
            returnHome()
          } else {
            LOG.info("Going to " + finder.color)
            move(finder.path)
          }
        }
        def returnHome() {
          LOG.info("going home")
          move(homeFinder.path)
        }

        LOG.debug("flag: " + tank.flag)
        tank.flag match {
          case None => findFlag()
          case Some(flag) => returnHome()
        }
      }
    }
  }

}

object PotentialFieldAgent extends AgentCreator {
  val LOG = org.apache.log4j.Logger.getLogger("cs470.agents.PotentialFieldAgent")

  def name = "pf"

  def create(host: String, port: Int) = new PotentialFieldAgent(host, port)
}
