package cs470.agents

import cs470.bzrc.Tank
import cs470.utils._
import Angle._

class DummyAgent(host: String, port: Int) extends Agent(host, port) with Threading {

  import DummyAgent._

  def run() {
    LOG.info("Running dummy agents")
    myTanks.foreach(moveDummyTank)
  }

  def moveDummyTank(tank: Tank) {
    LOG.info("Starting tank #" + tank.id + " on dummy path")

    //Go forward a bit, then rotate 60 degrees
    actor {
      loop {
        LOG.debug("Tank #" + tank.id + " is moving")
        tank.setSpeed(1.0)

        sleep(7000)

        LOG.debug("Tank #" + tank.id + " is stopping")
        tank.setSpeed(0.0)

        LOG.debug("Tank #" + tank.id + " is rotating")
        val (angle, time) = tank.moveAngle(degree(60))
        LOG.debug("Tank #%d rotated %s (in %d ms)".format(tank.id, angle.degree, time))

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
