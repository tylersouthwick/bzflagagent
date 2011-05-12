package cs470.movement.search

import java.util.{Comparator, PriorityQueue}
import cs470.domain.{Occupant, Point}

trait UniformCostSearcher extends Searcher with SearchVisualizer {

	final def g(n : Node) = n.cost
	def h(n : Node) : Double = 0
	def f(n : Node) = g(n) + h(n)

	def doSearch(start: Node) : Seq[Point] = {
		val frontier = new PriorityQueue[Node](10, new Comparator[Node] {
			def compare(o1: Node, o2: Node) = new java.lang.Double(f(o1)).compareTo(f(o2))
		})
		frontier.add(start)

		while (!frontier.isEmpty) {
			val node = frontier.poll()
			val children = node.filter(!_.visited).filter(!frontier.contains(_)).filter(_.occupant == Occupant.NONE)
			visualizer.drawSearchNodes(children map (child => (node.location, child.location)))
			if (isGoal(node)) return node.path
			node.visited = true
			children.foreach(frontier.add(_))
		}
		throw new IllegalArgumentException("didn't find it")
	}
}
