package cs470.movement

import search.{PenalizedUniformCostSearch, A_StarSearcher}
import cs470.domain.Constants._

import scala.math._
import cs470.bzrc.DataStore
import cs470.utils._
import cs470.domain._

object SearchPath {
	val LOG = org.apache.log4j.Logger.getLogger(classOf[SearchPath])
}

abstract class SearchPath(store: DataStore) extends PotentialFieldGenerator(store) {


	val searchTitle = ""
	val searchName = ""

	val searchGoal: Point
	val tankIdd: Int

	import SearchPath.LOG

	implicit object blah extends Ordering[((Point, Double), Int)] {
		def compare(x: ((Point, Double), Int), y: ((Point, Double), Int)) = {
			x._1._2 compareTo y._1._2
		}
	}

	def buildOccgrid(): Occgrid = {
		val worldSize: Int = store.constants("worldsize")
		val tankRadius: Double = store.constants("tankradius")
		new UsableOccgrid(Properties("discreteSize", 100), obstacles, tankRadius, worldSize, enemies)
	}

	trait AgentSearcher extends A_StarSearcher with PenalizedUniformCostSearch {
		val datastore = store
		lazy val goal = store.tanks.find(_.tankId == tankIdd).get.location
		val q = queue
		lazy val occgrid = buildOccgrid()
	}

	val searcher = new AgentSearcher {
		lazy val name = searchName
		lazy val tankId = tankIdd
		lazy val start = searchGoal
		lazy val title = searchTitle
		lazy val filename = searchName
	}

	lazy val result = searcher.search.reverse
	val r = Properties("searcher.r", 5)
	val s = Properties("searcher.s", 30)
	val alpha = Properties("searcher.alpha", 5.8)
	val futurePoints = Properties("searcher.futurePoints", 2)
	val previousPoints = Properties("searcher.previousPoints", 3)
	lazy val myTank = store.tanks.find(_.tankId == tankIdd).get

	def getPathVector(point: Point) = {
		val minPointIdxT = result.map {
			p => (p, p.distance(point))
		}.zipWithIndex.min._2

		val minPointIdx = {
if (result.length < minPointIdxT + 4) {
	minPointIdxT + 4
} else minPointIdxT
}

		val minPoint: Point = result(minPointIdx)
/*
		LOG.debug("Minpoint: " + minPointIdx)

				val forward = {
					val points = java.lang.Math.min(result.size - minPointIdx, futurePoints)
					if (points <= 0) {
						//			new Point(0, 0)
						AttractivePF(point, result(result.size - 1), 2, 20, 20)
					} else {
						val slice = result.slice(minPointIdx, minPointIdx + points)
						SearchPath.LOG.debug(slice)
						// new Vector(slice.foldLeft(new Point(0, 0))(_ + _) - point * points)
						slice.zipWithIndex.foldLeft(new Point(0, 0)) {
							case (vector, (p, idx)) =>
								vector + AttractivePF(point, p, r, s, alpha /*/ java.lang.Math.pow((idx + 1), 2)*/)
						}
					}
				}
        new Vector(forward)
*/

		val slice = result.slice(minPointIdx, result.size - 1)
		val deg60: Double = Degree(40).radian.value
		val startingLocation = myTank.location
		val startingAngle = myTank.angle.radian.value

		val x1 = minPoint
		var x2 = minPoint

		val goalPoint = try {
			slice.find {
				x4 =>
					val x3 = x2
					val num = (x2 - x1) * (x4 - x3)
					val den = (x2 - x1).magnitude * (x4 - x3).magnitude
					val theta = acos(num / den)
					x2 = x4
					val xs = Seq(x1, x2, x3, x4)
					theta > deg60
			}.get
		}
		catch {case _ =>
			result(result.size - 1)
		}
		val dist = point.distance(goalPoint)
        val diff = new Point(java.lang.Math.signum(random - .5) * dist * .04,0)
		val t = new Vector(AttractivePF(point, goalPoint+diff, 1, dist, 50))
		t

	}
}

