package cs470.bzrc

import cs470.domain.{Point, Flag}

class RefreshableFlags(queue : BzrcQueue) extends RefreshableData[Flag, RefreshableFlag](queue) {

	protected def convert(f: Flag) = new RefreshableFlag {
		def flag = findItem(_.color == color)

		def location = flag.location

		def possessingTeamColor = flag.possessingTeamColor

		val color = f.color
	}

	protected def loadData(con: BzFlagConnection) = con.flags
}

abstract class RefreshableFlag() {
	val color : String
	def possessingTeamColor  : String
	def location : Point

	override def toString = color + " @" + location
}

