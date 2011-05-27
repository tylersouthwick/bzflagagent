package cs470.domain

import org.junit.Test
import java.io.{InputStreamReader, BufferedReader}

object OccgridUnitTest {
	def createOccgrid = {
		val reader = new BufferedReader(new InputStreamReader(classOf[OccgridUnitTest].getResourceAsStream("/occgrid.dat")))
		val occgrid = new OccgridCommand
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
		val occgrid = OccgridUnitTest.createOccgrid
		val corners = occgrid.corners
		println("found " + corners.size + " corners")
		corners.foreach(x=>println(x))
	}
}
