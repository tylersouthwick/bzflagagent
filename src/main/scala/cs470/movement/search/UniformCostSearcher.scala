package cs470.movement.search

import java.util.{Comparator, PriorityQueue}

trait UniformCostSearcher extends Searcher with SearchVisualizer {

	def g(n : Node) : Double = n.cost

	def doSearch(start: Node) {
		val frontier = new PriorityQueue[Node](10, new Comparator[Node] {
			def compare(o1: Node, o2: Node) = new java.lang.Double(g(o1)).compareTo(g(o2))
		})
		frontier.add(start)

		while (!frontier.isEmpty) {
			val node = frontier.poll()
			visualizer.drawSearchNodes(node map (child => (node.location, child.location)))
			if (isGoal(node)) return
			node.visited = true
			node.filter(!_.visited).filter(!frontier.contains(_)).foreach(frontier.add(_))
		}
		throw new IllegalArgumentException("didn't find it")
	}
}