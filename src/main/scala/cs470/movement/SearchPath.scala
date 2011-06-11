package cs470.movement

import scala.math._
import cs470.bzrc.DataStore
import cs470.utils._
import cs470.domain._

object SearchPath {
	val LOG = org.apache.log4j.Logger.getLogger(classOf[SearchPath])

	val r = Properties("searcher.r", 5)
	val s = Properties("searcher.s", 30)
	val alpha = Properties("searcher.alpha", 5.8)
	val futurePoints = Properties("searcher.futurePoints", 2)
	val previousPoints = Properties("searcher.previousPoints", 3)
}

class SearchPath(store: DataStore, result: Seq[Point]) extends PotentialFieldGenerator(store) {

	implicit object blah extends Ordering[((Point, Double), Int)] {
		def compare(x: ((Point, Double), Int), y: ((Point, Double), Int)) = {
			x._1._2 compareTo y._1._2
		}
	}

	def getPathVector(point: Point) = {
		val minPointIdxT = result.map {
			p => (p, p.distance(point))
		}.zipWithIndex.min._2

		val minPointIdx = {
			if (result.length > minPointIdxT + 4) {
				minPointIdxT + 4
			} else minPointIdxT
		}

		val minPoint: Point = try{
			result(minPointIdx)
		} catch {
			case t : Throwable => {
				println(t)
				throw t
			}
		}

		val slice = result.slice(minPointIdx, result.size - 1)
		val deg60: Double = Degree(40).radian.value

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
					theta > deg60
			}.get
		} catch {
			case _ =>
				result(result.size - 1)
		}
		val dist = point.distance(goalPoint)
		val diff = new Point(java.lang.Math.signum(random - .5) * dist * .02, 0)
		val t = new Vector(AttractivePF(point, goalPoint + diff, 1, dist, 50))
		t
	}
}

