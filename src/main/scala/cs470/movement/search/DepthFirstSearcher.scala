package cs470.movement.search

import collection.mutable.Stack
import cs470.domain.Point

trait DepthFirstSearcher extends Searcher with SearchVisualizer {

	val path = new Stack[(Int, Int)]

	def doSearch(node : Node) {
		if (depthFirstSearch(node)) {
			val points = path.map(new Point(_))
			visualizer.drawFinalPath(points.zipWithIndex map {case (point, idx) => {
				if (idx == points.length) {
					(point, point)
				} else {
					(point, points(idx + 1))
				}
			}})
		} else {
			throw new IllegalStateException("did not find path")
		}
	}

	def depthFirstSearch(node : Node) : Boolean = {
		val vis_nodes = node filter(!_.visited) map (child => (new Point(node.location),new Point(child.location)))
		visualizer.drawSearchNodes(vis_nodes)
		node filter(!_.visited) foreach {child =>
			path.push(child.location)
			child.visited = true
			if (child.location._1 == end._1 && child.location._2 == end._2) {
				throw new Exception("MAYBE END?")
			} else {
				if (depthFirstSearch(child)) {
					return true
				}
			}
			path.pop()
		}
		false
	}

}