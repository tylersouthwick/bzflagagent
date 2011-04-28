package cs470.agents

import cs470.BzrcQueue
import cs470.domain.MyTank
import actors._
import Actor._
import java.lang.Math._

abstract class Agent(host: String, port: Int) {
  val queue = new BzrcQueue(host, port)

  def run

  implicit def tankSpeed(tank: MyTank) = new {
    def speed(s: Float) = queue.invoke(_.speed(tank.id, s))

    def setAngularVelocity(v: Float) = queue.invoke(_.angvel(tank.id, v))

    def updateTank = queue.invokeAndWait(_.mytanks.filter(_.id == tank.id).apply(0))

    def getAngle = {
      val angle = updateTank.angle
      if (angle < 0)
        2 * PI + angle
      else
        angle
    }

    def shoot = queue.invoke(_.shoot((tank.id)))

    def moveAngle(theta: Float) = {
      if (tank.status == "dead") {
        Agents.LOG.debug("Tried to rotate Tank #" + tank.id + " but it is dead")
      } else {
        computeAngle(theta)
      }
    }

    def computeAngle(theta: Float) {
      val angle = getAngle
      val finalAngle = {
        val tmp = angle + theta
        if (tmp > 2 * PI)
          (2 * PI - tmp).asInstanceOf[Float]
        else
          tmp.asInstanceOf[Float]
      }
      val Kp = 2f
      val Kd = 45f
      val tol = 1e-3f

      Agents.LOG.debug("Tank #1 rotating from " + angle + " to " + finalAngle)
      def pdController(diff2: Float) {
        val angle = getAngle
        val diff = finalAngle - angle
        val v = Kp * diff + Kd * (diff - diff2)

        if (v < tol) {
          setAngularVelocity(0f)
        } else {
          setAngularVelocity(v.asInstanceOf[Float])
          pdController(diff.asInstanceOf[Float])
        }
      }

      pdController(0.0f)

      def rad2deg(rad: Float) = {
        (rad * 180 / PI).asInstanceOf[Float]
      }

      def deg2rad(deg: Float) = {
        (deg * PI / 180).asInstanceOf[Float]
      }

      val finalDiff = getAngle - angle
      Agents.LOG.debug("Achieved an angle difference of: " + finalDiff.asInstanceOf[Float] + " rad (" + rad2deg(finalDiff.asInstanceOf[Float]) + " deg)")
    }

  }

  def timeout(milliseconds: => Long)(callback: => Unit) {
    actor {
      loop {
        reactWithin(milliseconds) {
          case TIMEOUT => callback
        }
      }
    }
  }
}

trait AgentCreator {
  def name: String

  def create(host: String, port: Int): Agent

  override def toString = name
}

object Agents {
  val all = Seq(DummyAgent)
  val LOG = org.apache.log4j.Logger.getLogger("cs470.agents.Agents")

  def start(agent: String, host: String, port: Int) {
    LOG.info("Creating agent: " + agent)
    val agents = all.filter(_.name == agent).map(_.create(host, port))
    LOG.info("Created agents " + agents)
    agents.foreach(_.run)
  }
}
