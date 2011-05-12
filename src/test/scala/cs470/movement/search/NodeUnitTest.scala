package cs470.movement.search

import cs470.domain.OccgridUnitTest
import org.junit.{Assert, Test}

class NodeUnitTest {

	@Test
	def nodeCreation() {
		val occgrid = OccgridUnitTest.createOccgrid
		val nodes = new Nodes(occgrid)

		val node1 = nodes(5, 5, 0)
		node1.visited = true

		val node2 = nodes(5, 5, 0)
		Assert.assertTrue(node2.visited)

		Assert.assertSame(node1, node2)
	}
}