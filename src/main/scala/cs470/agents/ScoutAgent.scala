package cs470.agents

import cs470.utils.Threading
import cs470.visualizer.BayesianVisualizer
import cs470.movement.{PotentialFieldsMover, SearchPath}
import cs470.domain._
import cs470.domain.Constants._
import cs470.visualization.PFVisualizer

class ScoutAgent(host: String, port: Int) extends Agent(host, port) with Threading {

	import ScoutAgent._

	def run() {
		LOG.info("Starting scout agent")

		val occgrid = new BayesianOccgrid with BayesianVisualizer {
			val constants = store.constants
		}
		occgrid.startVisualizer()

		myTanks foreach {
			tank =>
					actor {
						loop {
							occgrid.update(tank)
							sleep(1000)
						}
					}
				actor {
					//loop {
						def mygoal = new Point(0, 0)
						def searcher = new SearchPath(store, mygoal, tank.id, "scout_" + tank.tankId, "scout_" + tank.tankId) {
							override def buildOccgrid() = occgrid
						}
						val mytank = tank
						new PotentialFieldsMover(store) {
							def path = searcher.getPathVector(mytank.location)

							val goal = mygoal
							val tank = mytank
							override val moveWhileTurning = true
							override val howClose = 30
						}.moveAlongPotentialField()
					/*new PFVisualizer {
						val samples = 25
						val pathFinder = searcher
						val plotTitle = tank.callsign + " to safe point"
						val fileName = tank.callsign + "_safePoint"
						val name = tank.callsign + "SafePoint"
						val worldsize: Int = constants("worldsize")
						val obstacleList = obstacles
					}.draw()*/
					//}
					println("DONE!!!")
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