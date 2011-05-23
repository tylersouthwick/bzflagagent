package cs470.movement

import search.{PenalizedUniformCostSearch, A_StarSearcher}
import cs470.domain.{UsableOccgrid}
import cs470.domain.{Point,Vector}
import cs470.domain.Constants._

import scala.math._
import cs470.bzrc.DataStore

class SearchPath(store : DataStore) extends FindAgentPath(store) {
  val searchGoal = flags.find(_.color == "green").get.location - new Point(15,15)

  trait AgentSearcher extends A_StarSearcher with PenalizedUniformCostSearch {
    val worldSize : Int = store.constants("worldsize")
    val tankRadius : Double = store.constants("tankradius")

    val datastore = store
    val start = store.tanks(tankId).location
    val q = queue
    lazy val occgrid = new UsableOccgrid(100,obstacles,tankRadius,worldSize,enemies)

  }

  val searcher = new AgentSearcher {
    val name = "aStarSafePoint"
    val tankId = 0
    val goal = searchGoal
    val title = "To Safe point"
    val filename = "Safe point searcher"
  }

  lazy val result = searcher.search

  def getPathVector(point: Point) = {
    val r = result.foldLeft(new Point(0,0)){(v,p) =>
       if(point.distance(searchGoal) > p.distance(searchGoal)){
         v + regectivePF(point,p,4,10,.2)
       } else {
         v + AttractivePF(point,p,4,10,.5)
       }
    }
    new Vector(r)
  }

  def AttractivePF(current: Point, goal: Point, r1: Double, s: Double, alpha: Double) = {
    val r2 = r1 + s
    val as = alpha * s
    val d = current.distance(goal)
    val theta = current.getAngle(goal)

    if (d < r1)
      new Point(0, 0)
    else if (d > r2)
      new Point(0,0)//Point(as * cos(theta), as * sin(theta))
    else
      new Point(alpha * (d - r1) * cos(theta), alpha * (d - r1) * sin(theta))
  }

  def regectivePF(current: Point, goal: Point, r1: Double, s: Double, beta: Double) = {
    val r2 = r1 + s
    val d = current.distance(goal)
    val theta = current.getAngle(goal)
    val i = 30

    if (d < r1)
      new Point(-signum(cos(theta)) * i, -signum(sin(theta)) * i)
    else if (d > r2)
      new Point(0, 0)
    else
      new Point(-beta * (r2 - d) * cos(theta), -beta * (r2 - d) * sin(theta))
  }

}

