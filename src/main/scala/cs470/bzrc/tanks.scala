package cs470.bzrc

import cs470.domain.{Point, MyTank}
import java.util.Date
import cs470.utils.{Radian, Angle, Threading}

class RefreshableTanks(queue: BzrcQueue) extends RefreshableData[MyTank, Tank](queue) {
	private val LOG = org.apache.log4j.Logger.getLogger(classOf[RefreshableTanks])


	protected def loadData(con: BzFlagConnection) = con.mytanks
	protected def convert(f: MyTank) = buildTank(f.id)

	private def buildTank(buildTankId: Int) = new Tank(queue, this) {
		private def tank: MyTank = findItem(_.id == tankId)

		val tankId = buildTankId
		def angvel = tank.angvel
		def xy = tank.xy
		def vx = tank.vx
		def angle = tank.angle
		def location = tank.location
		def flag = tank.flag
		def timeToReload = tank.timeToReload
		def shotsAvailable = tank.shotsAvailable
		def status = tank.status
		def callsign = tank.callsign
	}

}

object Tank {
	val LOG = org.apache.log4j.Logger.getLogger(classOf[Tank])
}

import java.lang.Math._
import cs470.utils.Angle._

abstract class Tank(queue : BzrcQueue, tanks : RefreshableTanks) extends Threading {

	val tankId: Int

	def id = tankId

	def angvel: Double
	def xy: Double
	def vx: Double
	def angle: Angle
	def location: Point
	def flag: Option[String]
	def timeToReload: Double
	def shotsAvailable: Int
	def status: String
	def callsign: String
	def dead = "dead" == status

	def speed(s: Double) {
		queue.invoke {
			_.speed(tankId, s)
		}
	}

	def setAngularVelocity(v: Double) {
		queue.invoke{
			_.angvel(tankId, v)
		}
	}

	def shoot() {
		queue.invoke{
			_.shoot(tankId)
		}
	}

	import Tank.LOG

	def moveAngle(theta: Angle) = {
		if (dead) {
			LOG.debug("Tried to rotate Tank #" + tankId + " but it is dead")
			(degree(0), 0)
		} else {
			computeAngle(theta)
		}
	}

	def moveToAngle(targetAngle : Angle) {
		pdController(radian(0), targetAngle)
	}

	def getTime = (new Date).getTime

	def computeAngle(theta: Radian) = {
		val startingAngle = angle
		val targetAngle = startingAngle + theta

		val startTime = getTime
		pdController(radian(0), targetAngle)
		((angle - startingAngle), (getTime - startTime).asInstanceOf[Int])
	}

	val Kp = 1
	val Kd = 4.5
	val tol = degree(1).radian
	val tolv = .1
	val maxVel = .7854 //constants("tankangvel")

	def pdController(error0: Radian, targetAngle : Radian) {
		val uncorrectedError = (targetAngle - angle).radian

		println("angle: " + angle.degree)
		println("targetAngle: " + targetAngle.degree)
		//Correct for right turns
		val moddedError = uncorrectedError % (2 * PI)
		val errorM = moddedError % PI
		val error = new Radian(if (errorM == moddedError) errorM else -errorM)
		println("error: " + error.degree)

		val rv = (Kp * error + Kd * (error - error0) / 200);
		val v = if(rv > maxVel) 1 else rv/maxVel

		if (abs(error) < tol && abs(v) < tolv) {
			setAngularVelocity(0f)
		} else {
			//Agents.LOG.debug("Setting velocity to " + v)
			setAngularVelocity(v)
			tanks.waitForNewData()
			pdController(error, targetAngle)
		}
	}
}