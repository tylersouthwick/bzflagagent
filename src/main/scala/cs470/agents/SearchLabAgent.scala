package cs470.agents

import cs470.movement.search.{UniformCostSearcher, DepthFirstSearcher}

class SearchLabAgent(host : String, port : Int) extends Agent(host, port) {
	val tankId = 0
	lazy val grid = queue.invokeAndWait(_.occgrid(tankId))

	def run() {
		val team = constants("team")

		depthFirst.search()
	}

	lazy val greenFlag = store.flags.find(_.color == "green").get.location
	lazy val tank = store.tanks(tankId)

	def depthFirst = new DepthFirstSearcher {
		val datastore = store
		val filename = "depth_first.gpi"
		val start = tank.location
		val goal = greenFlag
		val occgrid = grid
	}

	def uniformCost = new UniformCostSearcher {
		val datastore = store
		val filename = "uniformCost.gpi"
		val start = tank.location
		val goal = greenFlag
		val occgrid = grid
	}
}

object SearchLabAgent extends AgentCreator {
	def create(host: String, port: Int) = new SearchLabAgent(host, port)

	def name = "search"
}

