package cs470.movement.search

import cs470.domain.{Point, Occgrid}
import collection.mutable.{Stack, HashMap, LinkedList}

/**
 * @author tylers2
 */

object Node {
	def apply(occgrid : Occgrid, x : Int, y : Int) = new Node(null, occgrid, x, y, 0)
}

class Node(val parent : Node, occgrid : Occgrid, x : Int, y : Int, val cost : Double) extends Traversable[Node] {

	val sqrt2 = java.lang.Math.sqrt(2)

	private def childrenPoints = {
		val left = (x - 1, y, 1.0)
		val right = (x+1, y, 1.0)
		val up = (x, y+1 , 1.0)
		val down =(x, y-1, 1.0)
		val upLeft = (x-1, y+1, sqrt2)
		val upRight = (x+1, y+1, sqrt2)
		val downLeft = (x - 1, y - 1, sqrt2)
		val downRight = (x + 1, y - 1, sqrt2)

		Seq(left, right, up, down, upLeft, upRight, downLeft, downRight)
	}

	lazy val children = childrenPoints.filter { node =>
				//verify that the node is within the grid
				val x = node._1
				val y = node._2
			(x >= 0 && x < occgrid.width) && (y >= 0 && y < occgrid.height)
		      }.map(t => new Node(this, occgrid, t._1, t._2, cost + t._3))

	def foreach[U](f: (Node) => U) {
		children.foreach(f)
	}

	def location = new Point(x + occgrid.offset._1, y + occgrid.offset._2)

	def gridLocation = (x, y)

	def occupant = occgrid.data(x)(y)

	def path : Seq[Point] = {
		val stack = new Stack[Point]
		var parent = this
		while (parent != null) {
			stack.push(parent.location)
			parent = parent.parent
		}
		stack
	}

	override def toString = "(" + x + ", " + y + ")"
}
