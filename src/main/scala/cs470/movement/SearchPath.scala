package cs470.movement

import search.{PenalizedUniformCostSearch, A_StarSearcher}
import cs470.domain.{UsableOccgrid}
import cs470.domain.{Point, Vector}
import cs470.domain.Constants._

import scala.math._
import cs470.bzrc.DataStore

class SearchPath(store: DataStore) extends FindAgentPath(store) {
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

  def getPathVector(point: Point) = {
    val r = result.map {
      p => (p, p.distance(point))
    }.zipWithIndex

    implicit object blah extends Ordering[((Point, Double), Int)] {
      def compare(x: ((Point, Double), Int), y: ((Point, Double), Int)) = {
        x._1._2 compareTo y._1._2
      }
    }

    val minPointIdx = r.min._2

    if (result.size > minPointIdx - 1) {
      new Vector(new Point(0, 0))
    } else {
      new Vector(result(minPointIdx + 1) - point)
    }
  }
}

