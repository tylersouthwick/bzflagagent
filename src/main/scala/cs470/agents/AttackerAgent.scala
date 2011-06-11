package cs470.agents

import cs470.bzrc.{DataStore, Tank}

/**
 * @author tylers2
 */

case class AttackerAgent(tank : Tank, store : DataStore) extends Agent(store) {

	def apply() {
		println(tank.callsign + " is ATTACKING!")
	}
}