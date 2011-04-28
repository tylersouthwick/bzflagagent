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

    def getAngle = updateTank.angle

    def shoot = queue.invoke(_.shoot((tank.id)))

    def moveAngle(theta: Float) = {
      if(tank.status == "dead"){
          Agents.LOG.debug("Tried to move Tank #" + tank.id + " but it is dead")
      } else {
          computeAngle(theta)
      }
    }

    def computeAngle(theta: Float) {
      val finalAngle = getAngle + theta;
      val Kp = .1f
      val Kd = 4.5f
      val tol = 1e-2

      def pdController(diff2 : Float) {
        val angle = getAngle
        val diff = finalAngle - angle
        val v = Kp * diff + Kd * (diff - diff2)

        setAngularVelocity(v)

        if (abs(angle-finalAngle) < tol) {

        } else {
          pdController(diff)
        }
      }

      pdController(0.0f)
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
