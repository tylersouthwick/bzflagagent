package cs470.movement.search

trait BreadthFirstSearcher extends DepthFirstSearcher {
	override val frontier =  new Frontier {
		val list = new java.util.LinkedList[Node]

		def get = list.poll

		def addNode(node: Node) {
			list.offer(node)
		}

		def isEmpty = list.isEmpty
	}


}
