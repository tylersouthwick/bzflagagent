package cs470.movement.search

import collection.mutable.{HashMap, LinkedList}
import cs470.domain.Occgrid

/**
 * @author tylers2
 */

class Nodes(occgrid : Occgrid) extends (((Int, Int)) => Node) {

	val nodes = new HashMap[(Int, Int), Node]

	def apply(location : (Int, Int)) = nodes.get(location) match {
		case Some(node) => node
		case None => {
			val node = new Node(occgrid, this, location._1, location._2)
			nodes(location) = node
			node
		}
	}

}

class Node(occgrid : Occgrid, nodes : Nodes, x : Int, y : Int) extends Traversable[Node] {

	var visited = false

	private def children = {
		val left = (x - 1, y)
		val right = (x+1, y)
		val up = (x, y+1)
		val down =(x, y-1)
		val upLeft = (x-1, y+1)
		val upRight = (x+1, y+1)
		val downLeft = (x - 1, y - 1)
		val downRight = (x + 1, y - 1)

		Seq(left, right, up, down, upLeft, upRight, downLeft, downRight)
	}

	def foreach[U](f: (Node) => U) {
		children.filter { node =>
				//verify that the node is within the grid
				val x = node._1
				val y = node._2
			(x >= 0 && x <= occgrid.width) && (y >= 0 && y <= occgrid.height)
		}.map(nodes).foreach(f)
	}

	def location = (x + occgrid.offset._1, y + occgrid.offset._2)

	def occupant = occgrid.data(x)(y)
}