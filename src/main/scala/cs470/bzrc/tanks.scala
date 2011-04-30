package cs470.bzrc

import cs470.domain.{Point, MyTank}
import cs470.utils.{Units, Threading}

class RefreshableTanks(queue: BzrcQueue) extends RefreshableData[Tank] {
	private var tanks: Seq[MyTank] = Seq[MyTank]()
	override lazy val availableData = lock {
		tanks.map(tank =>
				buildTank(tank.id)
		)
	}
	private val LOG = org.apache.log4j.Logger.getLogger(classOf[RefreshableTanks])

	schedule {
		LOG.debug("reloading tanks")
		val myTanks = queue.invokeAndWait(_.mytanks)
		LOG.debug("reload tanks")
		doLock {
			tanks = myTanks
		}
	}

	private def buildTank(buildTankId: Int) = new Tank(queue) {
		private def tank: MyTank = lock {
			tanks.filter(_.id == tankId).apply(0)
		}

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

abstract class Tank(queue : BzrcQueue) extends Threading with Units {

	val tankId: Int

	def id = tankId

	def angvel: Double
	def xy: Double
	def vx: Double
	def angle: Double
	def location: Point
	def flag: String
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

	def getAngle = {
		this.angle
		/*
		val angle = this.angle
		if (angle < 0)
			(2 * PI + angle).asInstanceOf[Double]
		else
			angle.asInstanceOf[Double]
		*/
	}

	def shoot() {
		queue.invoke{
			_.shoot(tankId)
		}
	}

	import Tank.LOG

	def moveAngle(theta: Double) = {
		if (dead) {
			LOG.debug("Tried to rotate Tank #" + tankId + " but it is dead")
			(0.0, 0)
		} else {
			computeAngle(theta)
		}
	}

	def computeAngle(theta: Double) = {
		val startingAngle = getAngle
		val targetAngle = startingAngle + theta
		val Kp = 1
		val Kd = 4.5
		val Ki = 0.0
		val tol = deg2rad(1)
		val tolv = .1
		val dt = 300;
		val maxVel = .7854 //constants("tankangvel")

		def getTime = java.util.Calendar.getInstance().getTimeInMillis
		def timeDifference(start: Long, end: Long) = (end - start).asInstanceOf[Int]

		def pdController(error0: Double, ierror: Double, time: Long) {
			val angle = getAngle
			val error = targetAngle - angle

			val rv = (Kp * error + Ki * ierror * dt + Kd * (error - error0) / 200);
			val v = if(rv > maxVel) 1 else rv/maxVel

			if (abs(error) < tol && abs(v) < tolv) {
				//Agents.LOG.debug("Setting velocity to 0")
				setAngularVelocity(0f)
			} else {
				//Agents.LOG.debug("Setting velocity to " + v)
				setAngularVelocity(v)
				sleep(dt)
				pdController(error, (ierror + error), getTime)
			}
		}

		val startTime = getTime
		pdController(0.0f, 0.0f, startTime)
		((getAngle - startingAngle), timeDifference(startTime, getTime))
	}
}