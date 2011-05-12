package cs470.movement.search

import cs470.domain.{Occupant, Point}
import java.util.LinkedList

trait IterativeDeepeningSearch extends Searcher {

	val title = "Iterative Deepening Depth First Search"
  private val limit = 1000000
  val LOG = org.apache.log4j.Logger.getLogger("cs470.movement.search.iddf")

	def doSearch(start: Node) : Seq[Point] = {
    (0 to limit).foreach{depth =>
      val result = start //replace this with a call to the DepthLimitedSearcher
      visualizer.flush
      if(isGoal(result)) return result.path
    }

    visualizer.close()
		throw new IllegalStateException("did not find path")

	}


}
