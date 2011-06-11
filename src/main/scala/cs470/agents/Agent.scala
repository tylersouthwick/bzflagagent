package cs470.agents
import cs470.bzrc.{Tank, DataStore}
import cs470.domain.Point
import cs470.movement.search.AStarSearch
import cs470.movement.SearchPath
import cs470.utils.{Threading, MovingPDController}

abstract class Agent(tank : Tank, store : DataStore) extends Threading {

	val constants = store.constants
	val flags = store.flags
	val myTanks = store.tanks
	val obstacles = store.obstacles
	val enemies = store.enemies
	val bases = store.bases
	val occgrid = store.occgrid

	val queue = store.queue

	def apply()

	final def start() {
		actor {
			println("test")
			apply()
			println("test2")
		}
	}

	final def move(goal : Point) {
		val path = AStarSearch(occgrid, tank, goal)
		val searchPath = new SearchPath(store, path)

		new MovingPDController(goal, tank, store) {
			def direction = searchPath.getPathVector(tank.location)
		}.move()
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
