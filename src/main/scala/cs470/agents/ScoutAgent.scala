package cs470.agents

import cs470.utils.Threading
import cs470.visualizer.BayesianVisualizer
import cs470.domain.{Point, BayesianOccgrid, Vector}
import cs470.movement.PotentialFieldGenerator._
import cs470.movement.{PotentialFieldsMover, PotentialFieldGenerator}
import cs470.domain.Constants._

class ScoutAgent(host: String, port: Int) extends Agent(host, port) with Threading {

	import ScoutAgent._

	def rand = scala.util.Random.nextGaussian()

	val stepSize = 50

	def run() {
		LOG.info("Starting scout agent")
		val worldsize: Int = constants("worldsize")

		val occgrid = new BayesianOccgrid with BayesianVisualizer {
			val constants = store.constants
		}

		occgrid.startVisualizer()

		myTanks foreach {
			myTank =>

				actor {
					def target: Point = {
						def findTarget(w: Double): Point = {
							val t = occgrid.getClosestUnexplored(myTank.location, w)
							if (t == null) {
								findTarget(w + stepSize)
							} else {
								t
							}
						}
						findTarget(stepSize)
					}


					val calculateFrequency = 10
					var count = 0
					var pseudoTarget: Point = target

					val searcher = new PotentialFieldGenerator(store) {
						def getPathVector(point: Point) = {
							if (count > calculateFrequency) {
								occgrid.update(myTank)
								count = 0
								pseudoTarget = target
								LOG.debug("sending " + myTank.callsign + " to " + pseudoTarget + " from " + myTank.location)
							}
							count = count + 1
							new Vector(AttractivePF(point, pseudoTarget, 5, 10, 20))
						}
					}

					new PotentialFieldsMover(store) {
						val tank = myTank

						def goal = pseudoTarget

						override val constantSpeed = .8

						def path = searcher.getPathVector(tank.location)
					}.moveAlongPotentialField()
				}
		}
		LOG.info("Scout agent done")
	}

}

object ScoutAgent extends AgentCreator {
	val LOG = org.apache.log4j.Logger.getLogger(classOf[ScoutAgent])

	def name = "scout"

	def create(host: String, port: Int) = new ScoutAgent(host, port)
}