package cs470.movement.search

import cs470.visualization.{SearchVisualizer => Visualizer}
import cs470.bzrc.DataStore
import cs470.domain.Constants._

trait SearchVisualizer extends Searcher {
	val filename: String
	val datastore: DataStore

	lazy val visualizer = {
		val tmp = new Visualizer {
			val fileName = filename
			val name = "Search Visualizer"
			val worldsize: Int = datastore.constants("worldsize")
			val obstacleList = datastore.obstacles
			val plotTitle = title
		}
		tmp.draw()
		tmp
	}

	override def search = {
		val points = super.search
		visualizer.drawFinalPath(points.zipWithIndex map {
			case (point, idx) => {
				if (idx + 1 < points.length) {
					(point, points(idx + 1))
				} else {
					(point, point)
				}
			}
		})

		points
	}
}
