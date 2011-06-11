package cs470.agents

import cs470.bzrc.{DataStore, Tank}

/**
 * @author tylers2
 */

case class Dalek(tank: Tank, store: DataStore) extends Agent(tank, store) {
	val LOG = org.apache.log4j.Logger.getLogger(classOf[Dalek])

	def apply() {
		LOG.info(tank.callsign + " is a dalek!")
		val enemy = enemies.getClosest(tank.location)
		LOG.info(tank.callsign + " is going for " + enemy.callsign)
		move(enemy.location)
	}
}