package cs470.movement

import cs470.bzrc.BzrcQueue
import cs470.domain.{Point,Vector}

abstract class FindAgentPath(q: BzrcQueue) {
  val enemies = q.invokeAndWait(_.othertanks)
  val obstacles = q.invokeAndWait(_.obstacles)
  val flags = q.invokeAndWait(_.flags)
  val bases = q.invokeAndWait(_.bases)

  def getPathVector(point: Point) : Vector
}