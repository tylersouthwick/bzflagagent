package cs470.agents

import cs470.movement.search.DepthFirstSearcher

class SearchLabAgent(host : String, port : Int) extends Agent(host, port) {
	lazy val grid = queue.invokeAndWait(_.occgrid(0))

	def run() {
		val team = constants("team")

		val searcher = depthFirst
		searcher.search()
	}

	def depthFirst = new DepthFirstSearcher {
		val datastore = store
		val filename = "depth_first.gpi"
		val start = store.tanks(0).location
		val goal = store.flags.find(_.color == "green").get.location
		val occgrid = grid
	}
}

object SearchLabAgent extends AgentCreator {
	def create(host: String, port: Int) = new SearchLabAgent(host, port)

	def name = "search"
}

