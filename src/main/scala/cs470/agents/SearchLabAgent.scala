package cs470.agents

import cs470.movement.search._
import cs470.utils.Properties

class SearchLabAgent(host : String, port : Int) extends Agent(host, port) {
	def run() {
		val team = constants("team")

		val searchers = Seq(aStar, uniformCost, depthFirst, breadthFirst, iterativeDeepening)
		println("Valid searchers: " + searchers)
		Properties("searchers") match {
			case None => {
				SearchLabAgent.LOG.warn("No searchers defined: running all")
				searchers.foreach(_.search())
			}
			case Some(prop) => {
				prop.split(",").foreach{d =>
					searchers.filter(_.name == d).foreach(_.search())
				}
			}
		}

		System.exit(-1)
	}

	lazy val greenFlag = store.flags.find(_.color == "green").get.location

	def depthFirst = new DepthFirstSearcher with AgentSearcher with SearcherName {
		val filename = "depth_first.gpi"
		val title = "Depth First Search"
		val name = "depthFirst"
	}

	def iterativeDeepening = new IterativeDeepeningSearch with AgentSearcher with SearcherName {
		val filename = "iterative_deepening.gpi"
		val name = "iterativeDeepening"
	}

	def uniformCost = {
		if (PenalizedUniformCostSearch.penalizedMode) {
			new LabUniformCostSearcher with PenalizedUniformCostSearch
		} else {
			new LabUniformCostSearcher
		}
	}

	class LabUniformCostSearcher extends UniformCostSearcher with AgentSearcher with SearcherName {
		val filename = "uniformCost.gpi"
		val title = "Uniform Cost Search"
		val name = "uniformCost"
	}

	def aStar = {
		if (PenalizedUniformCostSearch.penalizedMode) {
			new LabAStarSearcher with PenalizedUniformCostSearch
		} else {
			new LabAStarSearcher
		}
	}

	def breadthFirst = new BreadthFirstSearcher with AgentSearcher with SearcherName {
		val filename = "breadthFirst.gpi"
		val title = "Breadth First Search"
		val name = "breadthFirst"
	}

	trait AgentSearcher {
		val tankId = 0
		val datastore = store
		val start = store.tanks(tankId).location
		val goal = greenFlag
		val q = queue
	}

	trait SearcherName {
		val name : String

		override def toString = name
	}

	class LabAStarSearcher extends A_StarSearcher with AgentSearcher with SearcherName {
		val filename = "a_star.gpi"
		val title = "A* Search"
		val name = "aStar"
	}

}

object SearchLabAgent extends AgentCreator {
	val LOG = org.apache.log4j.Logger.getLogger(classOf[SearchLabAgent])

	def create(host: String, port: Int) = new SearchLabAgent(host, port)

	def name = "search"
}

