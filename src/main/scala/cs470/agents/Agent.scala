package cs470.agents

import cs470.BzrcQueue
import cs470.domain._
import actors._
import Actor._
import java.lang.Math._
import cs470.bzrc.RefreshableTanks

abstract class Agent(host: String, port: Int) {
  val queue = new BzrcQueue(host, port)

	val constants = queue.invokeAndWait(_.constants)
	val flags = queue.invokeAndWait(_.flags)
	val myTanks = new RefreshableTanks(queue)

  def run

  def rad2deg(rad: Float) = {
    (rad * 180 / PI).asInstanceOf[Float]
  }

  def deg2rad(deg: Float) = {
    (deg * PI / 180).asInstanceOf[Float]
  }

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

    def shoot {
		queue.invoke(_.shoot((tank.id)))
	}

    def moveAngle(theta: Float) = {
      if (tank.status == "dead") {
        Agents.LOG.debug("Tried to rotate Tank #" + tank.id + " but it is dead")
        (0f,0)
      } else {
        computeAngle(theta)
      }
    }

    def computeAngle(theta: Float) = {
      val startingAngle = getAngle
      val targetAngle = startingAngle + theta
      val Kp = 1f
      val Kd = 4.5f
      val Ki = 0.0f
      val tol = 5e-3f
      val tolv = 1e-1f
      val dt = 100;

      def getTime = java.util.Calendar.getInstance().getTimeInMillis
      def timeDifference(start: Long, end: Long) = (end - start).asInstanceOf[Int]

      //Agents.LOG.debug("Constants: " + queue.invokeAndWait(_.constants))

      //Agents.LOG.debug("Tank #" + tank.id + " rotating from " + angle + " to " + finalAngle)
      def pdController(error0: Float, ierror: Float, time: Long) {
        val angle = getAngle
        val error = targetAngle - angle
        //val dt = (getTime - time).asInstanceOf[Float]

        val v = Kp * error + Ki * ierror * dt + Kd * (error - error0) / dt

        //Agents.LOG.debug("Tank #%d error=%.3f".format(tank.id, abs(error)))
        if (abs(error) < tol && v < tolv) {
          setAngularVelocity(0f)
        } else {
          setAngularVelocity(v.asInstanceOf[Float])
          sleep(dt)
          pdController(error.asInstanceOf[Float], (ierror + error).asInstanceOf[Float], getTime)
        }
      }

      val startTime = getTime
      pdController(0.0f, 0.0f, startTime)

      val i1 = (getAngle - startingAngle).asInstanceOf[Float]
      val i2 = timeDifference(startTime, getTime)

      (i1,i2)
    }

  }

	def timeout(milliseconds : Long) (callback : => Unit) = receiveTimeout(milliseconds)(callback)

	def reactTimeout(milliseconds : Long)(callback: => Unit) {
		reactWithin(milliseconds) {
			case TIMEOUT => callback
		}
	}

	def receiveTimeout(milliseconds : Long)(callback: => Unit) {
		receiveWithin(milliseconds) {
			case TIMEOUT => callback
		}
	}

	def sleep(milliseconds : Long) = receiveTimeout(milliseconds) {}
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
