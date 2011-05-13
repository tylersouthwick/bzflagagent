package cs470.movement.search

import cs470.domain.Point
trait IterativeDeepeningSearch extends Searcher with DepthLimitedSearcher {

	val title = "Iterative Deepening Depth First Search"
	private val limit = 10000
	val LOG = org.apache.log4j.Logger.getLogger("cs470.movement.search.iddf")

	def doSearch(start: Node) : Node = {
		Seq(10, 100, 1000, 3000, 7000).foreach { depth =>
			if(depth % 50 == 0) LOG.debug("depth: " + depth)
			explored.clear()
      visualizer.clear()
			val result = depthSearch(start, depth)
			if (result != null && isGoal(result)) {
				println("final depth: " + result.depth)
				return result
			}
			System.gc()
		}

		visualizer.close()
		throw new IllegalStateException("did not find path")

	}


}
