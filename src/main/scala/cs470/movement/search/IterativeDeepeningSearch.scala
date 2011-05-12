package cs470.movement.search

import cs470.domain.{Occupant, Point}
import java.util.LinkedList

trait IterativeDeepeningSearch extends Searcher {

	val title = "Iterative Deepening Depth First Search"
  private val limit = 1000000
  val LOG = org.apache.log4j.Logger.getLogger("cs470.movement.search.iddf")

	def doSearch(start: Node) : Seq[Point] = {
    (0 to limit).foreach{depth =>
      val result = DepthLimitedSearch(start,depth)
      visualizer.flush()
      if(isGoal(result)) return result.path
    }

    visualizer.close()
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

//      node.visited = true

      visualizer.drawSearchNodes(node map (child => (node.location, child.location)))


      node filter(_.occupant == Occupant.NONE) foreach{child =>
        RecursiveDLS(child,depth - 1)
      }
    }

    return node
  }

}
