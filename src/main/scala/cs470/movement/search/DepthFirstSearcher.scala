package cs470.movement.search

import collection.mutable.Stack
import cs470.domain.{Occupant, Point}

trait DepthFirstSearcher extends Searcher with SearchVisualizer {

	def doSearch(node : Node) {
		val begin = time
		val points = depthFirstSearch(node)
		val end = time
		println("took " + (end - begin) + "ms")
		visualizer.drawFinalPath(points.zipWithIndex map {case (point, idx) => {
			if (idx + 1 < points.length) {
				(point, points(idx + 1))
			} else {
				(point, point)
			}
		}})
	}

	def depthFirstSearch(start: Node) : Seq[Point] = {
		val frontier = new Stack[Node]()
		frontier.push(start)
		val path = new Stack[Point]

		while (!frontier.isEmpty) {
			val node = frontier.pop()
			//visualizer.drawSearchNodes(node map (child => (node.location, child.location)))
			if (isGoal(node)) {
				println("found!")
				return buildPath(node)
			} else {
				node.visited = true
				node.filter(!_.visited).filter(!frontier.contains(_)).filter(_.occupant == Occupant.NONE).foreach(frontier.push)
			}
		}

		throw new IllegalStateException("did not find path")
	}

	def time = new java.util.Date().getTime

	def buildPath(node : Node) : Seq[Point] = {
		val stack = new Stack[Point]
		var parent = node
		while (parent != null) {
			stack.push(parent.location)
			parent = parent.parent
		}
		stack
	}
}
