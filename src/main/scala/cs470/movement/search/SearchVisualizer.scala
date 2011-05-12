package cs470.movement.search

import cs470.visualization.{SearchVisualizer => Visualizer}
import cs470.bzrc.DataStore
import cs470.domain.Constants._

trait SearchVisualizer {
	val filename : String
	val datastore : DataStore
	val title : String

	lazy val visualizer = new Visualizer(filename, datastore.obstacles, datastore.constants("worldsize"), title)


}