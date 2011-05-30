package cs470.agents

import cs470.visualizer.BayesianVisualizer
import cs470.domain._
import cs470.movement.PotentialFieldGenerator._
import cs470.domain.Constants._
import cs470.movement.{SearchPath, PotentialFieldsMover, PotentialFieldGenerator}
import collection.mutable.Queue
import cs470.utils._

class ScoutAgent(host: String, port: Int) extends Agent(host, port) with Threading {

	import ScoutAgent._

	def rand = scala.util.Random.nextGaussian()

	val stepSize = 150

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
					loop {
						occgrid.update(myTank)
						sleep(100)
					}
				}

				LOG.info("Filling queue for " + myTank.callsign)
				val pointsToVisit = new collection.mutable.Queue[(Int, Int)]()

//				val padding = 15
				//Fill Points
//				(padding to worldsize - padding).foreach {
//					x =>
//						(padding to worldsize - padding).foreach {
//							y =>
//								pointsToVisit.enqueue((x, y))
//						}
//				}
					pointsToVisit.enqueue((15,15))
				   pointsToVisit.enqueue(occgrid.convert(new Point(0,0)))

				while (pointsToVisit.size > 0) {
					val (x, y) = pointsToVisit.dequeue()

					val point = occgrid.getLocation(x, y)

					if (occgrid.P_s(x, y) == DefaultProperties.prior) {
						LOG.info("Trying point " + point + " [" + pointsToVisit.size + "]")
						occgrid.update(myTank)

							var searchType = "PF"

						def pfToPoint = new PotentialFieldGenerator(store) {
							searchType = "PF"
							def getPathVector(p: Point) = new Vector(AttractivePF(p, point, 3, 50, 100))
						}

						def aStarToPoint = new SearchPath(store) {
							searchType = "A*"
							LOG.info("Creating A* to " + point)
							val tankIdd = myTank.tankId
							val searchGoal = point

							override def buildOccgrid() = occgrid
						}

						var searchPath = pfToPoint

						val mover = new PotentialFieldsMover(store) {

							override def getTurningSpeed(angle: Double) : Double = {
								if(searchType == "A*"){
									if(angle > Degree(60).radian){
										0.0
//									} else if(angle > Degree(20).radian){
//										0.3
									} else {
										1.0
									}
								} else {
									super.getTurningSpeed(angle)
								}
							}

							def path = {
								searchPath.getPathVector(myTank.location)
							}

							val goal = point
							val (gx, gy) = occgrid.convert(goal)
							val tank = myTank

							override def inRange(vector: Vector) = {
								import scala.math._
								val angle: Double = myTank.angle.radian

								val angles = Seq.range(-15, 15, 2)

								val hitWall = if (searchType == "A*") {
									false
								} else {
									angles.foldLeft(false) {
										(t, d) =>
											val dangle: Double = Degree(d).radian
											val tmp = new Point(6 * cos(angle + dangle), 6 * sin(angle + dangle))

											val (x, y) = occgrid.convert(myTank.location + tmp)
											if (occgrid.data(x)(y) == Occupant.WALL) {
												true
											} else {
												false
											}
									}
								}

								if (hitWall) {
									LOG.info("Updating path for " + myTank.callsign + " with A* to " + point + " type=" + searchType)
									stop
									actor{
										LOG.info("Setting backward")
										tank.setSpeed(-1)
										sleep(3000)
										stop
									}
									searchPath = aStarToPoint
								}

								val isGoalInWall = occgrid.data(gx)(gy) == Occupant.WALL
								val isClose = myTank.location.distance(goal) < 30

								isGoalInWall || isClose
							}
						}

						mover.moveAlongPotentialField()
					}
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
