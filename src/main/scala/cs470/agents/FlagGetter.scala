package cs470.agents

import cs470.bzrc.{Tank, DataStore}
import cs470.utils.{Angle, MovingPDController}
import cs470.domain.{Point, Vector}

/**
 * @author tylers2
 */

class FlagGetter(position : Point, tank : Tank, store : DataStore) extends Agent(tank, store) {

	def apply() {
		gotoStartingPosition()
		gotoFlag()
	}

	def gotoStartingPosition() {
		println("going to: " + position)
		val searcher = findPFPath(position)
		val start = time

		new MovingPDController(position, tank, store) {
			def direction = searcher.getPathVector(tank.location)

		//	override def inRange(vector: Vector) = super.inRange(vector) || time - start - 10000 < 0
		}.move()
	}

	def gotoFlag() {
		println("going for flag")
		val goal = otherFlag.location
		println("the flag is: " + goal)
		val searcher = findPath(goal)

		new MovingPDController(goal, tank, store) {
			def direction = searcher.getPathVector(tank.location)

			override def inRange(vector: Vector) = otherFlag.possessingTeamColor == team
		}.move()
	}
}