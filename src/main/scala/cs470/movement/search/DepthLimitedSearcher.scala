package cs470.movement.search

import collection.mutable.Stack
import cs470.domain.Occupant

trait DepthLimitedSearcher extends Searcher {

  def depthSearch(start: Node, limit: Int): Node = {
    val frontier = new Frontier {
      val stack = Stack[Node]()

      def get = stack.pop()

      def addNode(node: Node) {
        stack.push(node)
      }

      def isEmpty = stack.isEmpty
    }
    frontier.push(start)

    while (!frontier.isEmpty) {
      val node = frontier.pop
      if (isGoal(node)) {
        println("found!")
        return node
      } else if (node.depth != limit) {
        val children = node.filter(!_.visited).filter(!frontier.contains(_)).filter(_.occupant == Occupant.NONE).filter(_.depth < limit)
        //visualizer.drawSearchNodes(children map (child => (node.location, child.location)))
        node.visited = true
        children.foreach(frontier.push)
      }
    }

    null
  }

}