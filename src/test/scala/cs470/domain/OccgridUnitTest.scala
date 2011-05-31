package cs470.domain

import org.junit.Test
import java.io.{InputStreamReader, BufferedReader}

object OccgridUnitTest {
	def createOccgrid(s : String) = {
		val reader = new BufferedReader(new InputStreamReader(classOf[OccgridUnitTest].getResourceAsStream("/" + s)))
		val occgrid = new OccgridCommand with PolygonFinder
		var line : String = reader.readLine
		while (line != null) {
			occgrid.read(line)
			line = reader.readLine
		}
		occgrid
	}
}

class OccgridUnitTest {

	@Test
	def findObstacles() {
		val occgrid = OccgridUnitTest.createOccgrid("small_maze1.occgrid")
		val corners = occgrid.polygons
		new cs470.visualization.Visualizer {
			val samples = 25
			val plotTitle = "obstacles"
			val fileName = "obstacles"
			val name = "obstacles"
			val worldsize = 400
			val obstacleList = corners
			draw()
			plotLines()
			close()
		}

	Runtime.getRuntime().exec(Array("gnuplot", "-persist", "obstacles_1.gpi")).waitFor()
		println("(width,height): " + (occgrid.width, occgrid.height))
		println("offset: " + occgrid.offset)
		println("found " + corners.size + " corners")
		corners.foreach(x=>println(x))
	}
}
