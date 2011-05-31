package cs470.movement.search

import cs470.utils.Properties

object PenalizedUniformCostSearch {
  val penalizedMode = Properties("penalized", true)
  val nextToNextTo = Properties("penalized.nextToNextTo", 2.5)
  val nextToAway = Properties("penalized.nextToAway", 1.1)
  val awayNextTo = Properties("penalized.awayNextTo", 2.3)
  val awayAway = Properties("penalized.awayAway", 1.0)
/*
  val penalizedMode = Properties("penalized", true)
  val nextToAway = Properties("penalized.nextToAway", 10.0)
  val nextToNextTo = Properties("penalized.nextToNextTo", 100.0)
  val awayNextTo = Properties("penalized.awayNextTo", 80.0)
  val awayAway = Properties("penalized.awayAway", 4.0)
*/
}

trait PenalizedUniformCostSearch extends UniformCostSearcher {

  import PenalizedUniformCostSearch._

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
