package cs470.agents

import cs470.movement.search.DepthFirstSearcher

class SearchLabAgent(host : String, port : Int) extends Agent(host, port) {
	def run() {
		val grid = queue.invokeAndWait(_.occgrid(0))
		val team = constants("team")

		val searcher = new DepthFirstSearcher {
			val datastore = store
			val filename = "depth_first.gpi"
			val start = store.tanks(0).location
			val goal = store.flags.find(_.color == "green").get.location
			val occgrid = grid
		}
		searcher.search()
	}
}

object SearchLabAgent extends AgentCreator {
	def create(host: String, port: Int) = new SearchLabAgent(host, port)

	def name = "search"
}

