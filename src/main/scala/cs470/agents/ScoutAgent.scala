package cs470.agents

import cs470.visualizer.BayesianVisualizer
import cs470.domain._
import cs470.movement.PotentialFieldGenerator._
import cs470.domain.Constants._
import cs470.movement.{SearchPath, PotentialFieldsMover, PotentialFieldGenerator}
import collection.mutable.Queue
import cs470.utils.{DefaultProperties, Threading}

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
				LOG.info("Filling queue for " + myTank.callsign)

				val pointsToVisit = new collection.mutable.Queue[(Int,Int)]()

				//Fill Points
				(0 to worldsize - 1).foreach{x=>
					(0 to worldsize - 1).foreach{y =>
						pointsToVisit.enqueue((x,y))
					}
				}

				while(pointsToVisit.size > 0) {
					val (x,y) = pointsToVisit.dequeue()

					val point = occgrid.getLocation(x,y)
					LOG.info((x,y) + " = " + occgrid.data(x)(y) + " [" + DefaultProperties.prior + "]")
					if(occgrid.P_s(x,y) == DefaultProperties.prior){
							LOG.info("Trying point " + point + " [" + pointsToVisit.size + "]")

						  val toPoint = new PotentialFieldGenerator(store){
							  def getPathVector(p: Point) = new Vector(AttractivePF(p,point,3,50,20))
						  }

							var count = 0

							val mover = new PotentialFieldsMover(store){
								def path = {
									if(count > 15){
										count = 0
										occgrid.update(myTank)
									}
									count = count + 1
									toPoint.getPathVector(myTank.location)
								}

								val goal = point
								val tank = myTank

								override def inRange(vector: Vector) = {
									myTank.location.distance(goal) > 30
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
