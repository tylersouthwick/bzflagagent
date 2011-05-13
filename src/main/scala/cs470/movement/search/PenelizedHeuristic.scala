package cs470.movement.search

import cs470.utils.Properties

trait PenelizedHeuristic extends HeuristicSearcher {

	val nextToNextTo = Properties("penelized.nextToNextTo", 1.5)
	val nextToAway = Properties("penelized.nextToAway", 1.1)
	val awayNextTo = Properties("penelized.awayNextTo", 1.3)
	val awayAway = Properties("penelized.awayAway", 1.0)

	override def h(n: Node) = super.h(n) * {
		val current = n.parent
		if (current.nextToOccupied) {
			if (n.nextToOccupied) {
				nextToNextTo
			} else {
				nextToAway
			}
		} else {
			if (n.nextToOccupied) {
				awayNextTo
			} else {
				awayAway
			}
		}
	}
}
