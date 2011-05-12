package cs470.movement.search

import java.util.{Comparator, PriorityQueue}
import cs470.domain.{Occupant, Point}

trait UniformCostSearcher extends Searcher with SearchVisualizer {

	final def g(n : Node) = n.cost
	def h(n : Node) : Double = 0
	def f(n : Node) = g(n) + h(n)


	val frontier = new Frontier {
		val frontier = new PriorityQueue[Node](10, new Comparator[Node] {
			def compare(o1: Node, o2: Node) = new java.lang.Double(f(o1)).compareTo(f(o2))
		})

		def addNode(node: Node) {
			frontier.add(node)
		}

		def isEmpty = frontier.isEmpty
		def pop = frontier.poll
	}

	def doSearch(start: Node) : Seq[Point] = {

		frontier.add(start)

		while (!frontier.isEmpty) {
			val node = frontier.pop
			if (node.parent != null)
				visualizer.drawSearchNodes(Seq((node.parent.location, node.location)))
			if (isGoal(node)) return node.path
			node.visited = true
			node.filter(!_.visited).filter(!frontier.contains(_)).filter(_.occupant == Occupant.NONE).foreach(frontier.add)
		}
		throw new IllegalArgumentException("didn't find it")
	}
}
