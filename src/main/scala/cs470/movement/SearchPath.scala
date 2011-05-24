package cs470.movement

import search.{PenalizedUniformCostSearch, A_StarSearcher}
import cs470.domain.{UsableOccgrid}
import cs470.domain.{Point, Vector}
import cs470.domain.Constants._

import scala.math._
import cs470.bzrc.DataStore
import cs470.utils._
import java.io.{File, FileOutputStream}

object SearchPath {
	val LOG = org.apache.log4j.Logger.getLogger(classOf[SearchPath])
}

class SearchPath(store: DataStore) extends PotentialFieldGenerator(store) {
  implicit object blah extends Ordering[((Point, Double), Int)] {
    def compare(x: ((Point, Double), Int), y: ((Point, Double), Int)) = {
      x._1._2 compareTo y._1._2
    }
  }

  val searchGoal = flags.find(_.color == "green").get.location

  trait AgentSearcher extends A_StarSearcher with PenalizedUniformCostSearch {
    val worldSize: Int = store.constants("worldsize")
    val tankRadius: Double = store.constants("tankradius")

    val datastore = store
    val start = store.tanks(tankId).location
    val q = queue
    lazy val occgrid = new UsableOccgrid(Properties("discreteSize", 100), obstacles, tankRadius, worldSize, enemies)

  }

  val searcher = new AgentSearcher {
    val name = "aStarSafePoint"
    val tankId = 0
    val goal = searchGoal
    val title = "To Safe point"
    val filename = "safePoint.gpi"
	  println("goal: " + occgrid.convert(searchGoal))

	  val o = new java.io.PrintWriter(new FileOutputStream(new File("world.dat")))
	  o.print(occgrid.print)
	  o.close()
  }

  lazy val result = searcher.search
  val r = Properties("searcher.r", 5)
  val s = Properties("searcher.s", 50)
  val alpha = Properties("searcher.alpha", .8)
	val futurePoints = Properties("searcher.futurePoints", 10)
	val previousPoints = Properties("searcher.previousPoints", 2)

  def getPathVector(point: Point) = {
    val minPointIdx = result.map {
      p => (p, p.distance(point))
    }.zipWithIndex.min._2

    SearchPath.LOG.debug("Minpoint: " + minPointIdx)

    val forward = {
		val points = java.lang.Math.min(result.size - minPointIdx, futurePoints)
		if (points == 0) {
			new Point(0, 0)
		} else {
			val slice = result.slice(minPointIdx, minPointIdx + points)
			SearchPath.LOG.debug(slice)
			// new Vector(slice.foldLeft(new Point(0, 0))(_ + _) - point * points)
			slice.zipWithIndex.foldLeft(new Point(0, 0)) { case (vector, (p, idx)) =>
				vector + AttractivePF(point, p, r, s, alpha / java.lang.Math.pow((idx + 1), 2))
			}
		}
	}

	  /*
	  val previous = {
		  val points = java.lang.Math.min(minPointIdx, previousPoints)
		  if (points == 0) {
			  new Point(0, 0)
		  } else {
			  val slice = result.slice(minPointIdx - points, minPointIdx)
			  // new Vector(slice.foldLeft(new Point(0, 0))(_ + _) - point * points)
			  slice.foldLeft(new Point(0, 0)) { (vector, p) =>
				  vector + ReflectivePF(point, p, r, s, alpha)
			  }
		  }
	  }
	  */

	  //val walls = getFieldForObstacles(point, 3, .2)

	  new Vector(forward + /*previous + walls + */randomVector)
  }
}
