package cs470

import bzrc.BzrcQueue

object Engine {
	def start(host : String, port : Int) = new Engine(host, port).start
}

class Engine(host : String, port : Int) {
	private val queue = new BzrcQueue(host, port)

	def start {
		val agent = new Agent(1)

		agent.speed(1)
		while(true) {}
	}

	/*
	def obstacles = con.obstacles
	def tanks = con.othertanks.filter(_.color == "red")
	val flag = con.flags.filter(_.color == "red").apply(0)
	*/

	class Agent(id : Int) {
		def speed(s : Double) = queue.invoke(_.speed(id, s))

		/*
		def potentialField = {
			obstacles.foreach { obstacle =>

			}
		}
		*/
	}

}

// vim: set ts=4 sw=4 et:
