package cs470.agents

import cs470.bzrc.{Tank, DataStore}
import cs470.movement.search.AStarSearch
import cs470.movement.{pfGotoPoint, SearchPath}
import cs470.domain._
import cs470.domain.Constants._
import cs470.utils.{Radian, Threading}

abstract class Agent(tank : Tank, store : DataStore) extends Threading {

	val constants = store.constants
	val flags = store.flags
	val myTanks = store.tanks
	val obstacles = store.obstacles
	val enemies = store.enemies
	val bases = store.bases
	val occgrid = store.occgrid
	val team = constants("team")

	val queue = store.queue

	val otherFlag = flags.filter(_.color != team).last

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
			val d = findAStarPath(goal)
			println("a*")
			d
		} catch {
			case _ => {
				println("pf")
				findPFPath(goal : Point)
			}
		}
	}

	final def findAStarPath(goal : Point) = {
		val path = AStarSearch(occgrid, tank, goal)
		new SearchPath(store, path)
	}

	final def findPFPath(goal : Point) = new pfGotoPoint(store, goal)
}

import java.lang.Math.PI
object Agent extends Threading {
	val LOG = org.apache.log4j.Logger.getLogger(classOf[Agent])

	def apply(store: DataStore) {
		LOG.info("Starting agents with " + store.tanks.size + " at our disposal")

		val worldsize : Int = store.constants("worldsize")
		val w = worldsize / 2 - 60

		val points = Seq((w, w), (-w, w), (w, -w), (-w, -w)).map(point => new Point(point._1, point._2))

		val decoys = store.tanks.filter(_.tankId < store.tanks.size - points.size)

		decoys.foreach(new Decoy(_, store).start())

		points.zipWithIndex.foreach{case (point, idx) =>
			new FlagGetter(point, store.tanks(decoys.size + idx), store).start()
		}

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
