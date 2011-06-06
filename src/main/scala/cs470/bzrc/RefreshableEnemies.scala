package cs470.bzrc

import cs470.domain.{Point, OtherTank}
import cs470.utils.Angle

class RefreshableEnemies(queue : BzrcQueue) extends RefreshableData[OtherTank, Enemy](queue) {

	def findData(data: BzData) = data.othertanks

	protected def convert(f: OtherTank) = new Enemy {
		def tank = findItem(_.callsign == callsign)

		def angle = tank.angle
		def location = tank.location
		def flag = tank.flag
		def status = tank.status
		def color = tank.color

		val callsign = f.callsign
	}
}

trait Enemy {
	val callsign : String
	def color : String
	def status : String
	def flag : String
	def location : Point
	def angle : Angle

	def alive = "alive" == status
	def dead = "dead" == status
}