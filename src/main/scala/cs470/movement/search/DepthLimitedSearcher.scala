package cs470.movement.search

import collection.mutable.Stack
import cs470.domain.{Occupant, Point}

trait DepthLimitedSearcher extends Searcher {

  val limit = 1

	def doSearch(start: Node) : Seq[Point] = {
		val frontier = new Frontier {
			val stack = Stack[Node]()

			def pop = stack.pop()

			def addNode(node: Node) {
				stack.push(node)
			}

			def isEmpty = stack.isEmpty
		}
		frontier.push(start)
		val path = new Stack[Point]

		while (!frontier.isEmpty) {
			val node = frontier.pop
			if (isGoal(node)) {
				println("found!")
				return node.path
			} else if(node.cost != limit) {
        val children = node.filter(!_.visited).filter(!frontier.contains(_)).filter(_.occupant == Occupant.NONE)
        visualizer.drawSearchNodes(children map (child => (node.location, child.location)))
				node.visited = true
				children.foreach(frontier.push)
			}
		}

		throw new IllegalStateException("did not find path")
	}

}