package cs470.agents

import cs470.utils._
import cs470.domain.Constants._
import cs470.domain.Point
import cs470.bzrc.{Tank, DataStore}
import cs470.movement.search.Searcher
import cs470.movement.{PotentialFieldGenerator, FindAgentPath, SearchPath, PotentialFieldsMover}

class MultiAgent(host: String, port: Int) extends Agent(host, port) with Threading {

	import MultiAgent._

	def run() {
		actor {
			new SniperAgent(store.tanks(0), store).apply()
		}
		actor {
			new DecoyAgent(store.tanks(1), store).apply()
		}
		LOG.info("running multi agent")
	}
}

abstract class MultiAgentBase(tank : Tank, store : DataStore) {

	val flags = store.flags
	val constants = store.constants
	val obstacles = store.obstacles
	val bases = store.bases

	val opponentFlag = bases.find(_.color == "green").get.points.center
	val mytank = tank

	val shotrange: Int = 200//constants("shotrange")

	val prePositionPoint : Point

	def gotoPrePosition() {
		val mover = {
			val toSafePoint = new SearchPath(store, prePositionPoint, tank.id)
			new cs470.visualization.PFVisualizer(toSafePoint, "toPrePosition_" + tank.id + ".gpi", obstacles, constants("worldsize"), 25)

			new PotentialFieldsMover(store) {
				def path = toSafePoint.getPathVector(mytank.location)

				val tank = mytank
			}
		}

		mover.moveAlongPotentialField()

		tank.setAngularVelocity(0)
		tank.speed(0)
		println("Stopping agent: " + tank.callsign)
	}

	def returnHome() {
		val goalFlag = bases.find(_.color == constants("team")).get.points.center

		val mover = {
			val toHomeBase = new SearchPath(store, goalFlag, tank.id)
			new cs470.visualization.PFVisualizer(toHomeBase, "toHomeBase.gpi", obstacles, constants("worldsize"), 25)

			new PotentialFieldsMover(store) {
				def path = toHomeBase.getPathVector(mytank.location)

				val tank = mytank
			}
		}


		mover.moveAlongPotentialField()
	}

	def apply() {
		//find current state
		gotoPrePosition()
	}
}

import cs470.domain.Vector

class SniperAgent(tank : Tank, store : DataStore) extends MultiAgentBase(tank, store) {
	val prePositionPoint = opponentFlag - new Point(shotrange, shotrange)

	override def apply() {
		super.apply()

	}
}

class DecoyAgent(tank : Tank, store : DataStore) extends MultiAgentBase(tank, store) {
	val prePositionPoint = opponentFlag - new Point(shotrange, 0)

	def alternate(dir : String, direction : Int) {
		println("going " + dir)

		println("prePositionPoint: " + prePositionPoint)
		println("opponentFlag: " + opponentFlag)
		val target = prePositionPoint + new Point(0, direction * 100)
		println("target: " + target)
		val searcher = new PotentialFieldGenerator(store) {
			def getPathVector(point: Point) = new Vector(target - point)
		}
		new cs470.visualization.PFVisualizer(searcher, "go_" + dir + ".gpi", obstacles, constants("worldsize"), 25)

		new PotentialFieldsMover(store) {
			val tank = mytank
			def path = searcher.getPathVector(tank.location)
		}.moveAlongPotentialField()
	}

	def north() {
		alternate("north", 1)
	}

	def south() {
		alternate("south", -1)
	}

	override def apply() {
		//super.apply()

		while (true) {
			north()
			south()
		}
	}
}

object MultiAgent extends AgentCreator {
	val LOG = org.apache.log4j.Logger.getLogger("cs470.agents.MultiAgent")

	def name = "multi"

	def create(host: String, port: Int) = new MultiAgent(host, port)
}
