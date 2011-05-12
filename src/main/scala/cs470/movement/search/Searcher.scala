package cs470.movement.search

import cs470.domain.{Point, Occgrid}
import cs470.bzrc.BzrcQueue

trait Searcher extends SearchVisualizer {

	val goal : Point
	val start : Point
	val tankId : Int
	val q : BzrcQueue

	def queue = q

	lazy val occgrid = {
		val o = queue.invokeAndWait(_.occgrid(tankId))
		o.addEnemies(datastore.enemies.filter(_.status != "dead") map(_.location))
		o
	}

	lazy val realStart = occgrid.convert(start)
	lazy val end = occgrid.convert(goal)

	final def search() {
		println("start: " + start)
		println("goal: " + goal)
		val nodes = new Nodes(occgrid)
		val begin = time
		val points = doSearch(nodes((realStart._1, realStart._2, 0, null)))
		val finished  = time
		println("took " + (finished - begin) + "ms")
		visualizer.drawFinalPath(points.zipWithIndex map {case (point, idx) => {
			if (idx + 1 < points.length) {
			(point, points(idx + 1))
			} else {
				(point, point)
			}
		}})
	}

	def doSearch(node : Node) : Seq[Point]

	def isGoal(node : Node) = node.gridLocation._1 == end._1 && node.gridLocation._2 == end._2

	def time = new java.util.Date().getTime
}