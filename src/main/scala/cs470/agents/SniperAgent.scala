package cs470.agents

import cs470.utils._
import cs470.domain.Constants._
import cs470.domain.Point
import cs470.bzrc.{Tank, DataStore}
import cs470.movement.{PotentialFieldGenerator, SearchPath, PotentialFieldsMover}

import cs470.domain.Vector

class SniperAgent(tank : Tank, store : DataStore) extends MultiAgentBase(tank, store) {
	val prePositionPoint = opponentFlag - new Point(shotrange, shotrange)

	override def apply() {
		super.apply()

	}
}

