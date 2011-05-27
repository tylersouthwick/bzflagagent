package cs470.agents

import cs470.utils.Threading
import cs470.visualizer.BayesianVisualizer
import cs470.domain.{Point, BayesianOccgrid, Vector}
import cs470.movement.PotentialFieldGenerator._
import cs470.domain.Constants._
import cs470.movement.{SearchPath, PotentialFieldsMover, PotentialFieldGenerator}

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


					val calculateFrequency = 20
					var count = 0
					var pseudoTarget: Point = target

					def pathSearcher = new SearchPath(store, myTank.id, myTank.callsign + "_toUnexplored", myTank.callsign + "_toUnexplored") {
						def searchGoal = {
							LOG.debug("Updating path " + myTank.callsign + " to " + pseudoTarget)
							pseudoTarget
						}

						override def buildOccgrid() = occgrid
					}

					var searcher = pathSearcher

					new PotentialFieldsMover(store) {
						val tank = myTank

						def goal = pseudoTarget

						override val constantSpeed = .8

						def path = {
							if (count > calculateFrequency) {
								count = 0
								pseudoTarget = target
								searcher = pathSearcher
								LOG.debug("Sending " + myTank.callsign + " to " + pseudoTarget + " from " + myTank.location)
							}
							count = count + 1

							if (count % 5 == 0)
								occgrid.update(myTank)

							try {
							searcher.getPathVector(tank.location)
							} catch {case _ => new Vector(new Point(0.0,0.0))}
						}
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