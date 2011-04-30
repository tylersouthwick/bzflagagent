package cs470.agents

import scala.actors._
import Actor._
import java.lang.Math.PI
import cs470.bzrc.Tank
import cs470.utils.{Units, Threading}

class DummyAgent(host: String, port: Int) extends Agent(host, port) with Threading with Units {

  import DummyAgent._

	def run() {
		LOG.info("Running dummy agent")
		val tanks = myTanks
		moveDummyTank(tanks.apply(1))
		moveDummyTank(tanks.apply(2))
	}

  def moveDummyTank(tank: Tank) = {
    LOG.info("Starting tank #" + tank.id + " on dummy path")

    //Go forward a bit, then rotate 60 degrees
    actor {
      loop {
        LOG.debug("Tank #" + tank.id + " is moving")
        tank.speed(1.0f)

        sleep(7000)

        LOG.debug("Tank #" + tank.id + " is stopping")
        tank.speed(0.0f)

        LOG.debug("Tank #" + tank.id + " is rotating")
        val (angle, time) = tank.moveAngle(60.0f * PI.asInstanceOf[Float] / 180.0f)
        LOG.debug("Tank #%d rotated %.1f deg (in %d ms)".format(tank.id.asInstanceOf[Int], rad2deg(angle), time))

      }
    }

    //Shoot every 1.5-2.5 seconds
    actor {
      loop {
        timeout(2000) {
          //LOG.debug("Tank #" + tank.id + " is shooting")
          tank.shoot()
        }
      }
    }
  }

}

object DummyAgent extends AgentCreator {
  val LOG = org.apache.log4j.Logger.getLogger("cs470.agents.DummyAgent")

  def name = "dummy"

  def create(host: String, port: Int) = new DummyAgent(host, port)
}
