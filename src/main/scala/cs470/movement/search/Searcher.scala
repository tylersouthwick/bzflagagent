package cs470.movement.search

import cs470.domain.Point
import cs470.bzrc.BzrcQueue
import cs470.utils.Properties
import java.io.{FileOutputStream, BufferedOutputStream, PrintWriter}

object Searcher {
  val LOG = org.apache.log4j.Logger.getLogger(classOf[Searcher])
}

trait Searcher extends SearchVisualizer {

  import Searcher.LOG

  val goal: Point
  val start: Point
  val tankId: Int
  val q: BzrcQueue
  val name: String

  def queue = q

  val explored = new java.util.HashSet[String]

  implicit def convertTuple(t: (Int, Int)) = t._1 + "_" + t._2

  implicit def exploring(node: Node) = new {
    def visited = explored.contains(convertTuple(node.gridLocation))

    def visited_=(value: Boolean) {
      val key = convertTuple(node.gridLocation)
      if (value)
        explored.add(key)
      else
        explored.remove(key)
    }
  }

  def occgrid: cs470.domain.Occgrid

  lazy val realStart = occgrid.convert(start)
  lazy val end = occgrid.convert(goal)

  final def search = {
    LOG.debug("Starting: " + title)
    val begin = time
    val result = doSearch(Node(occgrid, realStart._1, realStart._2))
    LOG.debug("Cost of " + title + ": " + result.cost + " (depth=" + result.depth + ")")
    val points = result.path
    val finished = time
    visualizer.drawFinalPath(points.zipWithIndex map {
      case (point, idx) => {
        if (idx + 1 < points.length) {
          (point, points(idx + 1))
        } else {
          (point, point)
        }
      }
    })
    LOG.debug(title + "took " + (finished - begin) + "ms")

    if (Properties("printNodes", false)) {
      val file = new PrintWriter(new BufferedOutputStream(new FileOutputStream(name + ".nodes.txt")))
      file.println("Cost:   " + result.cost)
      file.println("Length: " + result.depth)
      points.foreach {
        point =>
          file.println(point)
      }
      file.close()
    }

    points
  }

  def doSearch(node: Node): Node

  def isGoal(node: Node) = node.gridLocation._1 == end._1 && node.gridLocation._2 == end._2

  def time = new java.util.Date().getTime
}
