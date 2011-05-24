package cs470.movement

import search.{PenalizedUniformCostSearch, A_StarSearcher}
import cs470.domain.{UsableOccgrid}
import cs470.domain.{Point, Vector}
import cs470.domain.Constants._

import scala.math._
import cs470.bzrc.DataStore
import cs470.utils._

object SearchPath {
	val LOG = org.apache.log4j.Logger.getLogger(classOf[SearchPath])
}

class SearchPath(store: DataStore) extends PotentialFieldGenerator(store) {
  implicit object blah extends Ordering[((Point, Double), Int)] {
    def compare(x: ((Point, Double), Int), y: ((Point, Double), Int)) = {
      x._1._2 compareTo y._1._2
    }
  }

  val searchGoal = flags.find(_.color == "green").get.location - new Point(15, 15)

  trait AgentSearcher extends A_StarSearcher with PenalizedUniformCostSearch {
    val worldSize: Int = store.constants("worldsize")
    val tankRadius: Double = store.constants("tankradius")

    val datastore = store
    val start = store.tanks(tankId).location
    val q = queue
    lazy val occgrid = new UsableOccgrid(100, obstacles, tankRadius, worldSize, enemies)
  }

  val searcher = new AgentSearcher {
    val name = "aStarSafePoint"
    val tankId = 0
    val goal = searchGoal
    val title = "To Safe point"
    val filename = "safePoint.out"
  }

  lazy val result = searcher.search
  val r = Properties("searcher.r", 5)
  val s = Properties("searcher.s", 50)
  val alpha = Properties("searcher.alpha", .8)

  def getPathVector(point: Point) = {
    val minPointIdx = result.map {
      p => (p, p.distance(point))
    }.zipWithIndex.min._2

    SearchPath.LOG.debug("Minpoint: " + minPointIdx)

    val points = java.lang.Math.min(result.size - minPointIdx, 10)
    if (points == 0) {
      new Vector(new Point(0, 0))
    } else {
      val slice = result.slice(minPointIdx + 1, minPointIdx + points + 1)
      SearchPath.LOG.debug(slice)
     // new Vector(slice.foldLeft(new Point(0, 0))(_ + _) - point * points)
      val tmp = slice.foldLeft(new Point(0, 0)) { (vector, p) =>
        vector + AttractivePF(point, p, r, s, alpha)
      } + randomVector
      new Vector(tmp)
    }
  }
}

