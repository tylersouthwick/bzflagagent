package cs470.agents

import cs470.bzrc.{Tank, DataStore}
import cs470.utils.{MovingPDController}
import cs470.domain.{Point, Vector}

/**
 * @author tylers2
 */

object FlagGetter {
	val LOG = org.apache.log4j.Logger.getLogger(classOf[FlagGetter])
}

class FlagGetter(position : Point, tank : Tank, store : DataStore) extends Agent(tank, store) {

	import FlagGetter._

	def apply() {
		if (tank.location.distance(position) > 20) {
			gotoStartingPosition()
		}

		loop (tank.alive) {
			tank.flag match {
				case Some(color) => {
					goHome()
				}
				case _ => gotoFlag()
			}

			sleep(3000)
		}

		LOG.info(tank.callsign + " [FLAG GETTER] is DEAD")
	}

	def gotoStartingPosition() {
		LOG.info(tank.callsign + " going to starting position: " + position)
		val searcher = findPFPath(position)

        val start = time
        val timeout = 15000

		new MovingPDController(position, tank, store) {
			def direction = searcher.getPathVector(tank.location)
            override def inRange(vector : Vector) = super.inRange(vector) || (time - start - timeout > 0)
		}.move()
	}

	def gotoFlag() {
		LOG.info(tank.callsign + " is going for the " + otherFlag.color + " flag @" + otherFlag.location)
		val goal = otherFlag.location
		val searcher = findPath(goal)

		new MovingPDController(goal, tank, store) {
			def direction = searcher.getPathVector(tank.location)

			override def inRange(vector: Vector) = otherFlag.possessingTeamColor == team
		}.move()
	}

	def goHome() {
		LOG.info(tank.callsign + " has the flag and is going home")
		val goal = mybase.points.center
		val searcher = findPath(goal)

		new MovingPDController(goal, tank, store) {
			def direction = searcher.getPathVector(tank.location)

			//if i've been shot, stop moving
			override def inRange(vector: Vector) = super.inRange(vector) || tank.dead
		}.move()
	}
}
