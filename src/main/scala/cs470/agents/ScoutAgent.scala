package cs470.agents

import cs470.visualizer.BayesianVisualizer
import cs470.domain._
import cs470.movement.PotentialFieldGenerator._
import cs470.domain.Constants._
import cs470.movement.{SearchPath, PotentialFieldsMover, PotentialFieldGenerator}
import cs470.utils._
import java.util.Date
import cs470.visualization.PFVisualizer
import collection.mutable.{LinkedList, Queue}
import cs470.bzrc.Tank

class ScoutAgent(host: String, port: Int) extends Agent(host, port) with Threading {

	import ScoutAgent._

	def rand = scala.util.Random.nextGaussian()

	val stepSize = 150

	def run() {
		LOG.info("Starting scout agent")
		val occgrid = new BayesianOccgrid with BayesianVisualizer {
			val constants = store.constants
		}

		val pointsToVisit = {
			/*
			val worldsize: Int = constants("worldsize")
			val padding = 25
			val queue = new collection.mutable.Queue[(Int, Int)]
			(padding to worldsize - padding).foreach { x =>
				(padding to worldsize - padding).foreach { y =>
					queue.enqueue((x, y))
				}
			}
			queue
			*/
			val worldsize: Int = constants("worldsize")
			val queue = new collection.mutable.PriorityQueue[(String, Int, Int)]
			val padding = 25
			//	Fill Points
			val list = new java.util.LinkedList[(String, Int, Int)]
			(padding to worldsize - padding).foreach { x =>
				(padding to worldsize - padding).foreach { y =>
					list.add((java.util.UUID.randomUUID().toString, x, y))
				}
			}
			implicit object foo extends Ordering[(String, Int, Int)] {
				def compare(x: (String, Int, Int), y: (String, Int, Int)) : Int = x._1.compareTo(y._1)
			}
			import scala.collection.JavaConversions._
			list.foreach(t => queue.enqueue(t))
			LOG.info("points: " + queue.size)
			new {
				def dequeue() = {
					var t : (String, Int, Int) = null
					queue.synchronized {
						t = queue.dequeue()
					}
					(t._2, t._3)
				}
				def size = {
					var size = 0
					queue.synchronized {
						size = queue.size
					}
					size
				}
			}
		}

		occgrid.startVisualizer()

		val angles = Seq.range(-20, 20, 2).map(Degree(_).radian.value)

		def moveTank(myTank : Tank) {
				actor {
					loop {
						occgrid.update(myTank)
						sleep(300)
					}
				}

				while (pointsToVisit.size > 0) {
					val (x, y) = pointsToVisit.dequeue()

					val point = occgrid.getLocation(x, y)
					/*
					if (!occgrid.polygons.filter(_.contains(x, y)).isEmpty) {
						println("(" + x + ", " + y + ") is in a wall")
						occgrid.P_s(x, y, 1.0)
					}
					*/

					val ps = occgrid.P_s(x, y)
					if (ps < .95 && ps > .05) {
						println("Trying point " + point + " [" + pointsToVisit.size + "]")

						var searchType = "PF"

						def pfToPoint = new PotentialFieldGenerator(store) {
							searchType = "PF"

							def getPathVector(p: Point) = new Vector(AttractivePF(p, point, 3, 50, 100))
						}

						def aStarToPoint = new SearchPath(store) {
							searchType = "A*"
							LOG.debug("Creating A* to " + point)
							val tankIdd = myTank.tankId
							val searchGoal = point

							override def buildOccgrid() = occgrid
						}

						var searchPath = pfToPoint

						val mover = new PotentialFieldsMover(store) {

							override def getTurningSpeed(angle: Double): Double = {
								if (searchType == "A*") {
									if (angle > Degree(30).radian) {
										0.0
									} else {
										1.0
									}
								} else {
									super.getTurningSpeed(angle)
								}
							}

							def path = new Vector(searchPath.getPathVector(myTank.location).vector + PotentialFieldGenerator.randomVector)

							val goal = point
							val (gx, gy) = occgrid.convert(goal)
							val tank = myTank
							var lastPoint = tank.location

							override def inRange(vector: Vector) = {
								import scala.math._
								val angle: Double = myTank.angle.radian

								val hitWall = {
									angles.foldLeft(false) {
										(t, dangle) =>
											val tmp = new Point(8 * cos(angle + dangle), 8 * sin(angle + dangle))

											val (x, y) = occgrid.convert(myTank.location + tmp)
											if (occgrid.data(x)(y) == Occupant.WALL) {
												true
											} else {
												false
											}
									}
								}

								if (hitWall) {
									LOG.debug("Updating path for " + myTank.callsign + " with A* to " + point + " type=" + searchType)
									tank.setSpeed(-.5)
									sleep(1000)
									stop

									searchPath = aStarToPoint
									/*
									new PFVisualizer {
										val samples = 15
										val pathFinder = searchPath
										val plotTitle = "searchPath"
										val fileName = "searchPath"
										val name = "searchPath"
										val worldsize : Int = constants("worldsize")
										val obstacleList = Seq[Polygon]()
									}.draw()
									*/
								}
								def isInWall = {
									!occgrid.polygons.filter(_.contains(goal)).isEmpty
								}
								def isGoalInWall = occgrid.data(gx)(gy) == Occupant.WALL || isInWall
								val isClose = myTank.location.distance(goal) < 30

								isClose || isGoalInWall
							}
						}

						mover.moveAlongPotentialField()
					}
				}
		}
		myTanks foreach {myTank =>
			actor {
				moveTank(myTank)
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
