package cs470.movement.search

import cs470.bzrc.Tank
import cs470.domain.{Occgrid, Point}

trait A_StarSearcher extends UniformCostSearcher {
  override def h(n: Node) = new Point(end).distance(new Point(n.gridLocation))

	val title = "A*"
	val name = "A*"
}

object AStarSearch {
	def apply(occgrid : Occgrid, tank : Tank, goal : Point) = {
		val myoccgrid = occgrid
		val mygoal = goal
		new A_StarSearcher with PenalizedUniformCostSearch {
			val goal = mygoal
			val start = tank.location
			val tankId = tank.tankId

			def occgrid = myoccgrid
		}.search
	}
}