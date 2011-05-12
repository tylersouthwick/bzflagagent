package cs470.movement.search

import cs470.domain.Point
import cs470.bzrc.BzrcQueue

object Searcher {
	val LOG = org.apache.log4j.Logger.getLogger(classOf[Searcher])
}

trait Searcher extends SearchVisualizer {

	import Searcher.LOG

	val goal : Point
	val start : Point
	val tankId : Int
	val q : BzrcQueue

	def queue = q

	val explored = new java.util.HashSet[String]

	implicit def convertTuple(t : (Int, Int)) = t._1 + "_" + t._2

	implicit def exploring(node : Node) = new {
		def visited = explored.contains(convertTuple(node.gridLocation))
		def visited_=(value : Boolean) {
			val key = convertTuple(node.gridLocation)
			if (value)
				explored.add(key)
			else
				explored.remove(key)
		}
	}

	lazy val occgrid = {
		val o = queue.invokeAndWait(_.occgrid(tankId))
		o.addEnemies(datastore.enemies.filter(_.status != "dead") map(_.location))
		o
	}

	lazy val realStart = occgrid.convert(start)
	lazy val end = occgrid.convert(goal)

	final def search() {
		LOG.info("Starting: " + title)
		//println("start: " + start)
		//println("goal: " + goal)
		val begin = time
		val points = doSearch(Node(occgrid, realStart._1, realStart._2))
		val finished  = time
		visualizer.drawFinalPath(points.zipWithIndex map {case (point, idx) => {
			if (idx + 1 < points.length) {
			(point, points(idx + 1))
			} else {
				(point, point)
			}
		}})
		LOG.debug(title + "took " + (finished - begin) + "ms")
	}

	def doSearch(node : Node) : Seq[Point]

	def isGoal(node : Node) = node.gridLocation._1 == end._1 && node.gridLocation._2 == end._2

	def time = new java.util.Date().getTime
}
