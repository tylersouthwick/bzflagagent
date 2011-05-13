package cs470.movement.search

import cs470.utils.Properties

object PenelizedHeuristic {
	val penalizedMode = Properties("penelized", false)
	val nextToNextTo = Properties("penelized.nextToNextTo", 1.5)
	val nextToAway = Properties("penelized.nextToAway", 1.1)
	val awayNextTo = Properties("penelized.awayNextTo", 1.3)
	val awayAway = Properties("penelized.awayAway", 1.0)
}

trait PenelizedHeuristic extends UniformCostSearcher {

	import PenelizedHeuristic._

	override def f(n: Node) = super.f(n) * {
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
