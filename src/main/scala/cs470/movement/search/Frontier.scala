package cs470.movement.search

trait Frontier {

	val frontierNodes = new java.util.HashSet[String]

	def convertTuple(t : (Int, Int)) = t._1 + "_" + t._2

	def isEmpty : Boolean

	def contains(node : Node) = frontierNodes.contains(convertTuple(node.gridLocation))

	def add(node :Node) {
		frontierNodes.add(convertTuple(node.gridLocation))
		addNode(node)
	}

	def addNode(node : Node)

	def pop : Node

	def push(n : Node) {
		add(n)
	}
}
