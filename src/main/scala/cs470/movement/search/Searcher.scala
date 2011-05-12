package cs470.movement.search

import cs470.domain.{Point, Occgrid}

trait Searcher {

	val occgrid : Occgrid
	val goal : Point
	val start : Point

	protected lazy val end = occgrid.convert(goal)

	final def search() {
		println("start: " + start)
		println("goal: " + goal)
		println("end: " + end)
		val realStart = occgrid.convert(start)
		println("realStart: " + realStart)
		val nodes = new Nodes(occgrid)
		doSearch(nodes(realStart))
	}

	def doSearch(node : Node)
}