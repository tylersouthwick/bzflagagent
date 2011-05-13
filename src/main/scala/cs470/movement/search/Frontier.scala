package cs470.movement.search

trait Frontier {

	private val frontierNodes = new scala.collection.mutable.HashMap[String, Node]

	def convertTuple(t : (Int, Int)) = t._1 + "_" + t._2

	def isEmpty : Boolean

	//we only want a node that has the lowest cost
	def contains(node : Node) = {
		val key = convertTuple(node.gridLocation)
		frontierNodes.get(key) match {
			case None => false
			case Some(n) => {
				if (n.cost > node.cost) {
					frontierNodes.remove(key)
					false
				} else {
					true
				}
			}
		}
	}

	def add(node :Node) {
		frontierNodes(convertTuple(node.gridLocation)) = node
		addNode(node)
	}

	def addNode(node : Node)

	final def pop : Node = {
		val node = get
		frontierNodes.get(convertTuple(node.gridLocation)) match {
			case None => pop
			case Some(n) => node
		}
	}

	def get : Node

	def push(n : Node) {
		add(n)
	}
}
