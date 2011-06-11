package cs470.agents

import cs470.bzrc.DataStore
import cs470.utils.Threading

abstract class Agent(store: DataStore) extends Threading {

	val constants = store.constants
	val flags = store.flags
	val myTanks = store.tanks
	val obstacles = store.obstacles
	val enemies = store.enemies
	val bases = store.bases

	val queue = store.queue

	def apply()

	def start() {
		actor {
			println("test")
			apply()
			println("test2")
		}
	}
}

object Agent {
	val LOG = org.apache.log4j.Logger.getLogger(classOf[Agent])

	def apply(store: DataStore) {
		LOG.info("Starting agents with " + store.tanks.size + " at our disposal")
		for (tank <- store.tanks) {
			LOG.info("Starting " + tank.callsign)
			Dalek(tank, store).start()
		}
	}
}
