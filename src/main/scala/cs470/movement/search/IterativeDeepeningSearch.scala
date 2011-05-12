package cs470.movement.search

import cs470.domain.Point
trait IterativeDeepeningSearch extends Searcher with DepthLimitedSearcher {

	val title = "Iterative Deepening Depth First Search"
	private val limit = 10000
	val LOG = org.apache.log4j.Logger.getLogger("cs470.movement.search.iddf")

	def doSearch(start: Node): Seq[Point] = {
		(0 to limit).foreach { depth =>
			println("depth: " + depth)
			explored.clear()
			val result = depthSearch(start, depth)
			visualizer.clear()
			if (result != null && isGoal(result)) {
				println("final depth: " + result.depth)
				return result.path
			}
			//println("next depth")
			System.gc()
		}

		visualizer.close()
		throw new IllegalStateException("did not find path")

	}


}
