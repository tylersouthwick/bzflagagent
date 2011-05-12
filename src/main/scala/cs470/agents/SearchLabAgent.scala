package cs470.agents

import cs470.movement.search._

class SearchLabAgent(host : String, port : Int) extends Agent(host, port) {
	val tankId = 0
	lazy val grid = queue.invokeAndWait(_.occgrid(tankId))

	def run() {
		val team = constants("team")

		aStar.search()
		uniformCost.search()
		depthFirst.search()
		breadthFirst.search()
	}

	lazy val greenFlag = store.flags.find(_.color == "green").get.location
	lazy val tank = store.tanks(tankId)

	def depthFirst = new DepthFirstSearcher with AgentSearcher {
		val filename = "depth_first.gpi"
	}

	def uniformCost = new UniformCostSearcher with AgentSearcher {
		val filename = "uniformCost.gpi"
	}

	def aStar = new A_StarSearcher with AgentSearcher {
		val filename = "a_star.gpi"
	}

	def breadthFirst = new BreadthFirstSearcher with AgentSearcher {
		val filename = "breadthFirst.gpi"
	}

	trait AgentSearcher {
		val datastore = store
		val start = tank.location
		val goal = greenFlag
		val occgrid = grid
	}
}

object SearchLabAgent extends AgentCreator {
	def create(host: String, port: Int) = new SearchLabAgent(host, port)

	def name = "search"
}

