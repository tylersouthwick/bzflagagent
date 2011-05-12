package cs470.movement.search

import cs470.domain.{Occupant, Point}
import java.util.LinkedList

trait IterativeDeepeningSearch extends Searcher {

  private val limit = 1000000
  val LOG = org.apache.log4j.Logger.getLogger("cs470.movement.search.iddf")

	def doSearch(start: Node) : Seq[Point] = {
    (0 to limit).foreach{depth =>
      val result = DepthLimitedSearch(start,depth)
      if(isGoal(result)) return result.path
    }

		throw new IllegalStateException("did not find path")
	}

  def DepthLimitedSearch(start:Node, depth: Int) : Node = {
    LOG.debug("Looking at depth=" + depth)

    RecursiveDLS(start, depth)
  }

  def RecursiveDLS(node : Node, depth : Int)  : Node = {
    if(depth >= 0) {
      if(isGoal(node)){
        return node
      }

      node.visited = true

      node filter(!_.visited) filter(_.occupant == Occupant.NONE) foreach{child =>
        RecursiveDLS(child,depth - 1)
      }
    }

    return node
  }

}
