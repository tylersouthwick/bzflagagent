package cs470.agents

import cs470.movement.search._
import cs470.utils.Properties

class SearchLabAgent(host : String, port : Int) extends Agent(host, port) {
	val penelized = Properties("penelized", false)

	def run() {
		val team = constants("team")

		aStar.search()
		uniformCost.search()
		depthFirst.search()
		breadthFirst.search()
		iterativeDeepening.search()
	}

	lazy val greenFlag = store.flags.find(_.color == "green").get.location

	def depthFirst = new DepthFirstSearcher with AgentSearcher {
		val filename = "depth_first.gpi"
		val title = "Depth First Search"
	}

	def iterativeDeepening = new IterativeDeepeningSearch with AgentSearcher {
		val filename = "iterative_deepening.gpi"
	}

	def uniformCost = new UniformCostSearcher with AgentSearcher {
		val filename = "uniformCost.gpi"
		val title = "Uniform Cost Search"
	}

	def aStar = {
		if (penelized) {
			new LabAStarSearcher with PenelizedHeuristic
		} else {
			new LabAStarSearcher
		}
	}

	def breadthFirst = new BreadthFirstSearcher with AgentSearcher {
		val filename = "breadthFirst.gpi"
		val title = "Breadth First Search"
	}

	trait AgentSearcher {
		val tankId = 0
		val datastore = store
		val start = store.tanks(tankId).location
		val goal = greenFlag
		val q = queue
	}

	class LabAStarSearcher extends A_StarSearcher with AgentSearcher {
		val filename = "a_star.gpi"
		val title = "A* Search"
	}

}

object SearchLabAgent extends AgentCreator {
	def create(host: String, port: Int) = new SearchLabAgent(host, port)

	def name = "search"
}

