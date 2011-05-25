package cs470.movement.search

import collection.mutable.Stack
import cs470.domain.{Occupant, Point}

trait DepthFirstSearcher extends Searcher {

  val frontier: Frontier = new Frontier {
    val stack = new Stack[Node]()

    def get = stack.pop()

    def addNode(node: Node) {
      stack.push(node)
    }

    def isEmpty = stack.isEmpty
  }

  def doSearch(start: Node): Node = {
    frontier.push(start)
    val path = new Stack[Point]

    while (!frontier.isEmpty) {
      val node = frontier.pop
      val children = node.filter(!_.visited).filter(!frontier.contains(_)).filter(_.occupant == Occupant.NONE)
      visualizer.drawSearchNodes(children map (child => (node.location, child.location)))
      if (isGoal(node)) {
        return node
      } else {
        node.visited = true
        children.foreach(frontier.push)
      }
    }

    throw new IllegalStateException("did not find path")
  }

}
