package cs470.movement.search

import cs470.visualization.{SearchVisualizer => Visualizer}
import cs470.bzrc.DataStore
import cs470.domain.Constants._
import cs470.domain.Point

trait SearchVisualizer {
  val filename: String
  val datastore: DataStore
  val title: String

  lazy val visualizer = {
	  /*
    val tmp = new Visualizer {
      val fileName = filename
      val name = "Search Visualizer"
      val worldsize: Int = datastore.constants("worldsize")
      val obstacleList = datastore.obstacles
      val plotTitle = title
    }
    tmp.draw()
    tmp*/
	  new {
		  def close() {}
		  def clear() {}
		  def drawSearchNodes(s : Traversable[(Point, Point)]) {}
		  def drawFinalPath(s : Traversable[(Point, Point)]) {}
	  }
  }


}