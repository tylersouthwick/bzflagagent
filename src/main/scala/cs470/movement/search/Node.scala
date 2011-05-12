package cs470.movement.search

import collection.mutable.{HashMap, LinkedList}
import cs470.domain.{Point, Occgrid}

/**
 * @author tylers2
 */

class Nodes(occgrid : Occgrid) extends (((Int, Int, Double, Node)) => Node) {

	private val nodes = new HashMap[String, Node]

	implicit def makeKey(t : (Int, Int, Double, Node)) = t._1 + "_" + t._2

	def apply(location : (Int, Int, Double, Node)) = nodes.get(location) match {
		case Some(node) => node
		case None => {
			val node = new Node(location._4, occgrid, this, location._1, location._2, location._3)
			nodes(location) = node
			node
		}
	}

}

class Node(val parent : Node, occgrid : Occgrid, nodes : Nodes, x : Int, y : Int, val cost : Double) extends Traversable[Node] {

	var visited = false
	val sqrt2 = java.lang.Math.sqrt(2)

	private def childrenPoints = {
		val left = (x - 1, y, cost + 1, this)
		val right = (x+1, y, cost + 1, this)
		val up = (x, y+1 , cost + 1, this)
		val down =(x, y-1, cost + 1, this)
		val upLeft = (x-1, y+1, cost + sqrt2, this)
		val upRight = (x+1, y+1, cost + sqrt2, this)
		val downLeft = (x - 1, y - 1, cost + sqrt2, this)
		val downRight = (x + 1, y - 1, cost + sqrt2, this)

		Seq(left, right, up, down, upLeft, upRight, downLeft, downRight)
	}

	lazy val children = childrenPoints.filter { node =>
				//verify that the node is within the grid
				val x = node._1
				val y = node._2
			(x >= 0 && x < occgrid.width) && (y >= 0 && y < occgrid.height)
		}.map(nodes)

	def foreach[U](f: (Node) => U) {
		children.foreach(f)
	}

	def location = new Point(x + occgrid.offset._1, y + occgrid.offset._2)

	def gridLocation = (x, y)

	def occupant = occgrid.data(x)(y)

	override def toString = "(" + x + ", " + y + ")"
}
