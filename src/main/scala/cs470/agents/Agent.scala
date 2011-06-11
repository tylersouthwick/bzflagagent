package cs470.agents

import cs470.bzrc.DataStore

abstract class Agent(store: DataStore) {

	val constants = store.constants
	val flags = store.flags
	val myTanks = store.tanks
	val obstacles = store.obstacles
	val enemies = store.enemies
	val bases = store.bases

	val queue = store.queue

	def apply()

	def start() {
		apply()
	}
}

object Agent {
	def apply(store: DataStore) {
		for (tank <- store.tanks) {
			AttackerAgent(tank, store).start()
		}
		println("starting agents")
	}
}
