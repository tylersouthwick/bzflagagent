package cs470.agents

import cs470.BzrcQueue
import cs470.domain.MyTank

abstract class Agent(host : String, port : Int) {
  val queue = new BzrcQueue(host, port)

  def run

  implicit def tankSpeed(tank : MyTank) = new {
    def speed(s : Float) = queue.invoke(_.speed(tank.id,s))
  }
}

trait AgentCreator {
	def name : String
	def create(host : String, port : Int) : Agent
}

object Agents {
	val all = Seq(DummyAgent)

	def start(agent : String, host : String, port : Int) {
		all.filter(_.name == agent).foreach(_.create(host, port))
	}
}
