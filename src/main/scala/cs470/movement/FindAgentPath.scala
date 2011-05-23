package cs470.movement

import cs470.domain.{Point,Vector}
import cs470.bzrc.{DataStore, BzrcQueue}

abstract class FindAgentPath(val store : DataStore) {
	val enemies = store.enemies
	val obstacles = store.obstacles
	val flags = store.flags
	val bases = store.bases
	val tanks = store.tanks

	def getPathVector(point: Point) : Vector
}