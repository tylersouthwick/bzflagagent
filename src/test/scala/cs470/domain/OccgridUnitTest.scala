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
		val con = new cs470.bzrc.BzFlagConnection("localhost", 9000)
		val occgrid = con.occgrid(0)
		val corners = occgrid.corners
		println("(width,height): " + (occgrid.width, occgrid.height))
		println("offset: " + occgrid.offset)
		println("found " + corners.size + " corners")
		corners.foreach(x=>println(x))
	}
}
