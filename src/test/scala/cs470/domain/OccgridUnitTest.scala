package cs470.domain

import org.junit.Test
import java.io.{InputStreamReader, BufferedReader}

class OccgridUnitTest {

	@Test
	def buildGrid() {
		val reader = new BufferedReader(new InputStreamReader(classOf[OccgridUnitTest].getResourceAsStream("/occgrid.dat")))
		val occgrid = new Occgrid
		var line : String = reader.readLine
		while (line != null) {
			occgrid.read(line)
			line = reader.readLine
		}

		println(occgrid)
	}
}