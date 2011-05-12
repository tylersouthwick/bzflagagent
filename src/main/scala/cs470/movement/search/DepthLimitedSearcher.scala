package cs470.movement.search

import collection.mutable.Stack
import cs470.domain.{Occupant, Point}

trait DepthLimitedSearcher extends Searcher {

	def doSearch(start: Node) : Seq[Point] = {
		val frontier = new Stack[Node]()
		frontier.push(start)
		val path = new Stack[Point]

		while (!frontier.isEmpty) {
			val node = frontier.pop()
			val children = node.filter(!_.visited).filter(!frontier.contains(_)).filter(_.occupant == Occupant.NONE)
			visualizer.drawSearchNodes(children map (child => (node.location, child.location)))
			if (isGoal(node)) {
				println("found!")
				return node.path
			} else {
				node.visited = true
				children.foreach(frontier.push)
			}
		}

		throw new IllegalStateException("did not find path")
	}

}