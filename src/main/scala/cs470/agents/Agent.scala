package cs470.agents
import cs470.bzrc.{Tank, DataStore}
import cs470.movement.search.AStarSearch
import cs470.utils.{Threading, MovingPDController}
import cs470.movement.{pfGotoPoint, SearchPath}
import cs470.domain.{Vector, Point}
import cs470.domain.Constants._
import sun.java2d.pipe.LoopPipe
import javax.management.remote.rmi._RMIConnection_Stub

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

	final def time = new java.util.Date().getTime

	final def start() {
		actor {
			apply()
		}
	}

	final def findPath(goal : Point) = {
		println("finding new path: "+ goal)
		try {
			val d = doAStar(goal)
			println("a*")
			d
		} catch {
			case _ => {
				println("pf")
				doPotentialField(goal : Point)
			}
		}
	}

	private def doAStar(goal : Point) = {
		val path = AStarSearch(occgrid, tank, goal)
		new SearchPath(store, path)
	}

	private def doPotentialField(goal : Point) = new pfGotoPoint(store, goal)
}

object Agent extends Threading {
	val LOG = org.apache.log4j.Logger.getLogger(classOf[Agent])

	def apply(store: DataStore) {
		LOG.info("Starting agents with " + store.tanks.size + " at our disposal")
		store.tanks.filter(_.tankId < 8).foreach(new Decoy(_, store).start())
		//store.tanks.filter(_.tankId >= 8).foreach(new GotoFlag(_, store).start())

		actor {
			loop {
				for (tank <- store.tanks) {
					tank.shoot()
				}
				sleep(400)
			}
		}
	}
}
