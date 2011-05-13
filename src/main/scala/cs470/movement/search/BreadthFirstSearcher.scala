package cs470.movement.search

import cs470.domain.{Occupant, Point}
import java.util.LinkedList

trait BreadthFirstSearcher extends Searcher {

	def doSearch(start: Node) : Node = {
		val frontier = new Frontier {
			val list = new LinkedList[Node]()

			def get = list.pop

			def addNode(node: Node) {
				list.push(node)
			}

			def isEmpty = list.isEmpty
		}

		frontier.push(start)

		while (!frontier.isEmpty) {
			val node = frontier.pop
			visualizer.drawSearchNodes(node map (child => (node.location, child.location)))
			if (isGoal(node)) {
				println("found!")
				return node
			} else {
				node.visited = true
				node.filter(!_.visited).filter(!frontier.contains(_)).filter(_.occupant == Occupant.NONE).foreach(frontier.push)
			}
		}

		throw new IllegalStateException("did not find path")
	}

}
