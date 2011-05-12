package cs470.movement.search

import cs470.domain.{Occupant, Point}
import java.util.LinkedList

trait IterativeDeepeningSearch extends Searcher {

  private val limit = 100000

	def doSearch(start: Node) : Seq[Point] = {
    (0 to limit).foreach{depth =>
      val result = DepthLimitedSearch(start,depth)
      if(isGoal(result)) return result.path
    }

		throw new IllegalStateException("did not find path")
	}

  def DepthLimitedSearch(start:Node, depth: Int) : Node = {
    RecursiveDLS(start, depth)
  }

  def RecursiveDLS(node : Node, depth : Int)  : Node = {
    if(depth >= 0) {
      if(isGoal(node)){
        return node
      }

      node.foreach{child =>
        RecursiveDLS(child,depth - 1)
      }
    }
    return node
  }

}
