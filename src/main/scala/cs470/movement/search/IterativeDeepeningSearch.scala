package cs470.movement.search

import cs470.domain.Point
trait IterativeDeepeningSearch extends Searcher with DepthLimitedSearcher {

	val title = "Iterative Deepening Depth First Search"
	private val limit = 10000
	val LOG = org.apache.log4j.Logger.getLogger("cs470.movement.search.iddf")

	def doSearch(start: Node) : Node = {
		(1 to 20).map(_ * 500).foreach { depth =>
			LOG.debug("depth: " + depth)
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
