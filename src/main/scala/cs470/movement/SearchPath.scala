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

abstract class SearchPath(store: DataStore, tankIdd: Int, searchTitle: String, searchName: String) extends PotentialFieldGenerator(store) {

	def searchGoal : Point

	import SearchPath.LOG

	implicit object blah extends Ordering[((Point, Double), Int)] {
		def compare(x: ((Point, Double), Int), y: ((Point, Double), Int)) = {
			x._1._2 compareTo y._1._2
		}
	}

	def buildOccgrid() : Occgrid = {
		val worldSize: Int = store.constants("worldsize")
		val tankRadius: Double = store.constants("tankradius")
		new UsableOccgrid(Properties("discreteSize", 100), obstacles, tankRadius, worldSize, enemies)
	}

	trait AgentSearcher extends A_StarSearcher with PenalizedUniformCostSearch {
		val datastore = store
		val start = store.tanks.find(_.tankId == tankIdd).get.location
		val q = queue
		lazy val occgrid = buildOccgrid()
	}

	val searcher = new AgentSearcher {
		val name = searchName
		val tankId = 0
		val goal = searchGoal
		val title = searchTitle
		val filename = searchName

//		if (LOG.isDebugEnabled) {
			val newPoint = occgrid.convert(searchGoal)
			if (occgrid.data(newPoint._1)(newPoint._2) != Occupant.NONE) LOG.error("GOAL IS IN AN OBJECT")
			LOG.info("goal [" + searchGoal + "]: " + occgrid.convert(searchGoal) + " {size of occgrid: " + occgrid.width + "}")
			LOG.debug("goal: " + occgrid.convert(searchGoal))
//		}
	}

	lazy val result = searcher.search
	val r = Properties("searcher.r", 5)
	val s = Properties("searcher.s", 30)
	val alpha = Properties("searcher.alpha", 5.8)
	val futurePoints = Properties("searcher.futurePoints", 2)
	val previousPoints = Properties("searcher.previousPoints", 2)

	def getPathVector(point: Point) = {
		val minPointIdx = result.map {
			p => (p, p.distance(point))
		}.zipWithIndex.min._2 + 5

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
	}
}

