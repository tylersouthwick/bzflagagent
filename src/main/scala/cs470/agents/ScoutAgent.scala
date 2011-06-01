package cs470.agents

import cs470.visualizer.BayesianVisualizer
import cs470.domain._
import cs470.movement.PotentialFieldGenerator._
import cs470.domain.Constants._
import cs470.movement.{SearchPath, PotentialFieldsMover, PotentialFieldGenerator}
import cs470.utils._
import java.util.Date
import cs470.visualization.PFVisualizer
import collection.mutable.{PriorityQueue, LinkedList, Queue}
import java.awt.event.ItemEvent
import cs470.bzrc.{RefreshableData, Tank}
import javax.management.remote.rmi._RMIConnection_Stub

class ScoutAgent(host: String, port: Int) extends Agent(host, port) with Threading {

	import ScoutAgent._

	def rand = scala.util.Random.nextGaussian()

	val stepSize = 150

	def run() {
		LOG.info("Starting scout agent")
		val occgrid = new BayesianOccgrid with BayesianVisualizer {
			val constants = store.constants
		}

		val pointsToVisit : Queue[(Int, Int)] = {
			val worldsize: Int = constants("worldsize")
			val padding = 25
			val queue = new collection.mutable.Queue[(Int, Int)]
			(padding to worldsize - padding).foreach { x =>
				(padding to worldsize - padding).foreach { y =>
					queue.enqueue((x, y))
				}
			}
			queue
/*
			val worldsize: Int = constants("worldsize")
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
			//make sure it divides
			val tankCount = myTanks.size
			val map = list.zipWithIndex.groupBy{ case (item, idx) => idx % tankCount }.map{ case (count, data) =>
				val queue = new collection.mutable.PriorityQueue[(String, Int, Int)]
				data.foreach(t => queue.enqueue(t._1))
				queue
			}
			val list2 = new java.util.LinkedList[PriorityQueue[(String, Int, Int)]]
			map.foreach(p => list2.add(p))
			list2
*/
		}

		occgrid.startVisualizer()

		val angles = Seq.range(-40, 40, 1).map(t => Degree(t / 2).radian.value)

		def moveTank(myTank : Tank) {
			var usePF = true
				while (pointsToVisit.size > 0) {
					val (x, y) = pointsToVisit.dequeue()
					//println("dequeued: " + (x, y))

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

						def aStarToPoint = new SearchPath(store) {
							searchType = "A*"
							LOG.debug("Creating A* for " + myTank.callsign + " to " + point)
							val tankIdd = myTank.tankId
							val searchGoal = point

							override def buildOccgrid() = occgrid
						}

						def pfToPoint = {
							if (usePF) {
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

                            var previousDistance = new Point(1000.0,1000)

							var count = 0
							def path = {
								if (count > 8) {
                                    val dis = tank.location.distance(previousDistance)
                                    if(dis < 1){
                                    println("Tank " + tank.callsign + " appears stuck")
									tank.setSpeed(0)
									tank.setAngularVelocity(0)
									sleep(1000)
									tank.setSpeed(-1)
                            println("test")
									sleep(2000)
                            println("test1")
									tank.setSpeed(0)
									sleep(1000)
									RefreshableData.waitForNewData()

                                    println("Moving")
									RefreshableData.waitForNewData()
                                    println("recalculating")
									searchPath = aStarToPoint
                                }
                                    previousDistance = tank.location
									count = 0
								} else {
									count = count + 1
								}

								val tankLocation = myTank.location
								val walls = occgrid.neighbors(occgrid.convert(tankLocation)).filter{case (x, y) => occgrid.data(x)(y) == Occupant.WALL}.map{case (x, y) => occgrid.getLocation(x, y)}
								val wallField = walls.map(PotentialFieldGenerator.ReflectivePF(tankLocation, _, 5, 25, 500)).foldLeft(Point.ORIGIN) { _ + _ }
								try {
									new Vector(searchPath.getPathVector(tankLocation).vector + PotentialFieldGenerator.randomVector + wallField)
								} catch {
									case t:IllegalArgumentException => {
										occgrid.P_s(x, y, 1.0)
										new Vector(new Point(0, 0))
									}
								}
							}

							val goal = point
							val (gx, gy) = occgrid.convert(goal)
							val tank = myTank
							var lastPoint = tank.location

							override def inRange(vector: Vector) = {
								import scala.math._
								val angle: Double = myTank.angle.radian

								val hitWall = false
/*{
                                    val ad = 7
									val t = angles.foldLeft(0) {
										(t, dangle) =>
											val tmp = new Point(ad * cos(angle + dangle), ad * sin(angle + dangle))

											val (x, y) = occgrid.convert(myTank.location + tmp)
											if (occgrid.data(x)(y) == Occupant.WALL) {
												t+1
											} else {
                                                t
											}
									}
                                    t > angles.size / 2
								}
*/
								if (hitWall) {
									LOG.debug("Updating path for " + myTank.callsign + " with A* to " + point + " type=" + searchType)
                                    println("Setting " + tank.callsign + " backward")
                            
									tank.setSpeed(0)
									tank.setAngularVelocity(0)
									sleep(1000)
									tank.setSpeed(-.2)
									sleep(1000)
									tank.setSpeed(0)
									sleep(1000)
									RefreshableData.waitForNewData()

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
								def isInWall = false/*{
									!occgrid.polygons.filter(_.contains(goal)).isEmpty
								}*/
								def isGoalInWall = occgrid.data(gx)(gy) == Occupant.WALL || isInWall
								val isClose = myTank.location.distance(goal) < 30
								//println("isGoalInWall: " + isGoalInWall)
								//println("isClose: " + isClose)

								if (isClose || isGoalInWall) {
println("is close or is wall")
true
} else false
							}
						}

						mover.moveAlongPotentialField()
					}
				}
		}
		val ids = myTanks.map(_.tankId)

		schedule(300) {
			occgrid.update(queue.invokeAndWait(_.occgrids(ids)))
		}

		myTanks foreach {myTank =>
			actor {
				moveTank(myTank)//, pointsToVisit(myTank.tankId))
				LOG.info("Scout agent [" + myTank.callsign + "] is done")
			}
		}
	}

}

object ScoutAgent extends AgentCreator {
	val LOG = org.apache.log4j.Logger.getLogger(classOf[ScoutAgent])

	def name = "scout"

	def create(host: String, port: Int) = new ScoutAgent(host, port)
}
