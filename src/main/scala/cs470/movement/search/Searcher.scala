package cs470.movement.search

import cs470.domain.{Point, Occgrid}

trait Searcher {

	val occgrid : Occgrid
	val goal : Point
	val start : Point

	lazy val realStart = occgrid.convert(start)
	protected lazy val end = occgrid.convert(goal)

	final def search() {
		println("start: " + start)
		println("goal: " + goal)
		println("end: " + end)
		println("realStart: " + realStart)
		val nodes = new Nodes(occgrid)
		doSearch(nodes((realStart._1, realStart._2, 0, null)))
	}

	def doSearch(node : Node)

	def isGoal(node : Node) = node.gridLocation._1 == end._1 && node.gridLocation._2 == end._2
}