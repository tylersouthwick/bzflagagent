package cs470.movement.search

trait Frontier {

	private val frontierNodes = new scala.collection.mutable.HashMap[String, Node]

	def convertTuple(t : (Int, Int)) = t._1 + "_" + t._2

	implicit def convertNode(node : Node) = convertTuple(node.gridLocation)

	def isEmpty : Boolean

	//we only want a node that has the lowest cost
	def contains(node : Node) = {
		frontierNodes.get(node) match {
			case None => false
			case Some(n) => {
				if (n.cost > node.cost) {
					frontierNodes.remove(node)
					false
				} else {
					true
				}
			}
		}
	}

	def add(node :Node) {
		frontierNodes(node) = node
		addNode(node)
	}

	def addNode(node : Node)

	final def pop : Node = {
		val node = get
		frontierNodes.get(node) match {
			case None => pop
			case Some(n) => node
		}
	}

	def get : Node

	def push(n : Node) {
		add(n)
	}
}
