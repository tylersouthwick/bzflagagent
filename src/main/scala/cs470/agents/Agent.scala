package cs470.agents
import cs470.bzrc.{Tank, DataStore}
import cs470.domain.Point
import cs470.movement.search.AStarSearch
import cs470.movement.SearchPath
import cs470.utils.MovingPDController

abstract class Agent(tank : Tank, store : DataStore) {

	val constants = store.constants
	val flags = store.flags
	val myTanks = store.tanks
	val obstacles = store.obstacles
	val enemies = store.enemies
	val bases = store.bases
	val occgrid = store.occgrid

	val queue = store.queue

	def apply()

	def start() {
		apply()
	}

	def move(goal : Point) {
		val path = AStarSearch(occgrid, tank, goal)
		val searchPath = new SearchPath(store, path)

		new MovingPDController(goal, tank, store) {
			def direction = searchPath.getPathVector(tank.location)
		}.move()
	}
}

object Agent {
	def apply(store: DataStore) {
		println("starting agents")
		for (tank <- store.tanks) {
			println("tank: " + tank)
			AttackerAgent(tank, store).start()
		}
	}
}
