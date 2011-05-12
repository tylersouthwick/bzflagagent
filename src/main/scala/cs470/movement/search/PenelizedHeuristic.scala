package cs470.movement.search

trait PenelizedHeuristic extends HeuristicSearcher {

	override def h(n: Node) = super.h(n) * {
		val current = n.parent
		if (current.nextToOccupied) {
			if (n.nextToOccupied) {
				1.5
			} else {
				1.1
			}
		} else {
			if (n.nextToOccupied) {
				1.3
			} else {
				1.0
			}
		}
	}
}
