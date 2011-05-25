package cs470.movement.search

import cs470.domain.Point

trait A_StarSearcher extends UniformCostSearcher {
  override def h(n: Node) = new Point(end).distance(new Point(n.gridLocation))
}