package cs470.agents

import cs470.domain._
import cs470.domain.Constants._
import cs470.movement.{SearchPath, PotentialFieldsMover, PotentialFieldGenerator}
import cs470.utils._
import collection.mutable.Queue
import cs470.visualization.BayesianVisualizer
import cs470.bzrc.{DataStore, RefreshableData, Tank}

class ScoutAgent(store : DataStore) extends Agent(store) with Threading {

	import ScoutAgent._

	def rand = scala.util.Random.nextGaussian()

	val stepSize = 150

	def apply() {
		LOG.info("Starting scout agent")
		val occgrid = new BayesianOccgrid with BayesianVisualizer {
			val constants = store.constants
		}

		val pointsToVisit: Queue[(Int, Int)] = {
			val worldsize: Int = constants("worldsize")
			val padding = 10
			val queue = new collection.mutable.Queue[(Int, Int)]
			(padding to worldsize - padding).foreach {
				x =>
					(padding to worldsize - padding).foreach {
						y =>
							queue.enqueue((x, y))
					}
			}
			queue
		}

		occgrid.startVisualizer()

		def moveTank(myTank: Tank) {
			var usePF = true
			while (pointsToVisit.size > 0) {
				val (x, y) = pointsToVisit.dequeue()
				val point = occgrid.getLocation(x, y)

				val ps = occgrid.P_s(x, y)
				if (ps < .95 && ps > .05) {
					LOG.info("Tank " + myTank.callsign + " trying point " + point + " [" + pointsToVisit.size + "]")

					var searchType = "PF"

					def aStarToPoint = new SearchPath(store) {
						searchType = "A*"
						LOG.info("Creating A* for " + myTank.callsign + " to " + point)
						val tankIdd = myTank.tankId
						val searchGoal = point

						override def buildOccgrid() = occgrid
					}

					def pfToPoint = {
						if (usePF) {
						LOG.info("Using potential fields for " + myTank.callsign + " to " + point)
							usePF = false
							new PotentialFieldGenerator(store) {
								searchType = "PF"

								def getPathVector(p: Point) = new Vector(AttractivePF(p, point, 3, 50, 100))
							}
						} else aStarToPoint
					}

					var searchPath = pfToPoint

					val mover = new PotentialFieldsMover(store) {

						override def getTurningSpeed(angle: Double): Double = {
							if (searchType == "A*") {
								if (angle > Degree(20).radian) {
									0.1
								} else {
									.6
								}
							} else {
								super.getTurningSpeed(angle)
							}
						}

						var previousDistance = new Point(1000.0, 1000)

						var count = 0

						def path = {
							if (count > 8) {
								val dis = tank.location.distance(previousDistance)
								if (dis < 1) {
									LOG.info("Tank " + tank.callsign + " appears stuck")
									tank.setSpeed(0)
									tank.setAngularVelocity(.8)
									sleep(500)
									tank.setAngularVelocity(0)
									sleep(500)
									tank.setSpeed(-1)
									sleep(2000)
									tank.setSpeed(0)
									sleep(1000)
									RefreshableData.waitForNewData()

									LOG.debug("Moving")
									RefreshableData.waitForNewData()
									LOG.debug("recalculating")
									searchPath = aStarToPoint
								}
								previousDistance = tank.location
								count = 0
							} else {
								count = count + 1
							}

							val tankLocation = myTank.location
							val walls = occgrid.neighbors(occgrid.convert(tankLocation)).filter {
								t => occgrid.data(t._1)(t._1) == Occupant.WALL
							}.map {
								t => occgrid.getLocation(t._1, t._2)
							}
							val wallField = walls.map(PotentialFieldGenerator.ReflectivePF(tankLocation, _, 5, 25, 500)).foldLeft(Point.ORIGIN) {
								_ + _
							}
							try {
								new Vector(searchPath.getPathVector(tankLocation).vector + PotentialFieldGenerator.randomVector + wallField)
							} catch {
								case t: IllegalArgumentException => {
									LOG.info("Tank " + myTank.callsign + " is trying to find path in wall.  Updating all neighbor points")
									val updateValue = .96
									occgrid.P_s(x, y, updateValue)
									var count = 0
									//set all adjacent to 1
									def updateNeighbors(allNeighbors: Seq[(Int, Int)]) {
										//println("all neighbors: " + allNeighbors.map{t => (t, occgrid.data(t._1)(t._2))})
										val neighbors = allNeighbors.filter {
											t => occgrid.data(t._1)(t._2) != Occupant.WALL && occgrid.P_s(t._1, t._2) >= DefaultProperties.prior
										}
										//println("updating neighbors: " + neighbors)
										neighbors.foreach {
											t => occgrid.P_s(t._1, t._2, updateValue)
										}
										if (count < 150) {
											count = count + 1
											neighbors.foreach {
												t => updateNeighbors(occgrid.neighbors(t._1, t._2))
											}
										}
									}
									updateNeighbors(occgrid.neighbors(x, y))
									new Vector(new Point(0, 0))
								}
							}
						}

						val goal = point
						val (gx, gy) = occgrid.convert(goal)
						val tank = myTank
						var lastPoint = tank.location

						override def inRange(vector: Vector) = {
							def isGoalInWall = occgrid.data(gx)(gy) == Occupant.WALL
							val isClose = myTank.location.distance(goal) < 20
							if (isClose || isGoalInWall) {
								LOG.info("Tank " + myTank.callsign + " is close or is wall")
								true
							} else false
						}
					}

					mover.moveAlongPotentialField()
				}
			}
		}
		val ids = myTanks.map(_.tankId)

		schedule(100) {
			occgrid.update(queue.invokeAndWait(_.occgrids(ids)))
		}

		def time = (new java.util.Date).getTime
		myTanks foreach {
			myTank =>
				actor {
					val start = time
					moveTank(myTank)
					val end = time
					LOG.info("Scout agent [" + myTank.callsign + "] is done in " + (end - start) + "ms")
				}
		}
	}

}

object ScoutAgent {
	val LOG = org.apache.log4j.Logger.getLogger(classOf[ScoutAgent])
}
