package cs470.agents

import cs470.bzrc.{DataStore, Tank}
import cs470.movement.search.{AStarSearch, A_StarSearcher}

/**
 * @author tylers2
 */

case class AttackerAgent(tank : Tank, store : DataStore) extends Agent(tank, store) {

	def apply() {
		println(tank.callsign + " is ATTACKING!")
		move(flags.find(_.color == "green").get.location)
	}
}