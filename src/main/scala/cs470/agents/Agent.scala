package cs470.agents

import cs470.BzrcQueue
import cs470.domain.MyTank
import actors._
import Actor._

abstract class Agent(host: String, port: Int) {
  val queue = new BzrcQueue(host, port)

  def run

  implicit def tankSpeed(tank: MyTank) = new {
    def speed(s: Float) = queue.invoke(_.speed(tank.id, s))

    def shoot = queue.invoke(_.shoot((tank.id)))
  }

  def timeout(milliseconds : => Long)(callback : => Unit) {
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
	def name : String
	def create(host : String, port : Int) : Agent

	override def toString = name
}

object Agents {
	val all = Seq(DummyAgent)
  val LOG = org.apache.log4j.Logger.getLogger("cs470.agents.Agents")

	def start(agent : String, host : String, port : Int) {
    LOG.info("Creating agent: " + agent)
		val agents = all.filter(_.name == agent).map(_.create(host, port))
    LOG.info("Created agents " + agents)
    agents.foreach(_.run)
	}
}
