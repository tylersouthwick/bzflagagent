package cs470.movement.search

import cs470.domain.{Occupant, Point}
import collection.mutable.Queue

trait BreadthFirstSearcher extends Searcher {
  val LOG = org.apache.log4j.Logger.getLogger("cs470.movement.search.BreadthFirstSearcher")

  def doSearch(start: Node): Node = {
    val frontier = new Frontier {
      val list = new Queue[Node]()

      def get = list.dequeue

      def addNode(node: Node) {
        list.enqueue(node)
      }

      def isEmpty = list.isEmpty
    }

    frontier.addNode(start)

    var count = 0
    while (!frontier.isEmpty) {
      count = if (count == 10000) {
        System.gc()
        LOG.debug("Called garbage collector")
        0
      } else {
        count + 1
      }
      val node = frontier.pop
      visualizer.drawSearchNodes(node map (child => (node.location, child.location)))
      if (isGoal(node)) {
        LOG.info("Found goal!")
        return node
      } else {
        node.visited = true
        node.filter(!_.visited).filter(!frontier.contains(_)).filter(_.occupant == Occupant.NONE).foreach(frontier.addNode)
      }
    }

    throw new IllegalStateException("did not find path")
  }

}
