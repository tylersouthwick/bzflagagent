package cs470.bzrc

import cs470.domain.{Point, MyTank}
import cs470.utils.{Units, Threading}

class RefreshableTanks(queue: BzrcQueue) extends RefreshableData with Traversable[Tank] {
	private var tanks: Seq[MyTank] = Seq[MyTank]()
	private lazy val availableTanks = lock {
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

	def foreach[U](f: (Tank) => U) {
		availableTanks.foreach(tank => f(tank))
	}
}

object Tank {
	val LOG = org.apache.log4j.Logger.getLogger(classOf[Tank])
}

import java.lang.Math._

abstract class Tank(queue : BzrcQueue) extends Threading with Units {

	val tankId: Int

	def id = tankId

	def angvel: Float
	def xy: Float
	def vx: Float
	def angle: Float
	def location: Point
	def flag: String
	def timeToReload: Float
	def shotsAvailable: Int
	def status: String
	def callsign: String
	def dead = "dead" == status

	def speed(s: Float) {
		queue.invoke {
			_.speed(tankId, s)
		}
	}

	def setAngularVelocity(v: Float) {
		queue.invoke{
			_.angvel(tankId, v)
		}
	}

	def getAngle = {
		this.angle
		/*
		val angle = this.angle
		if (angle < 0)
			(2 * PI + angle).asInstanceOf[Float]
		else
			angle.asInstanceOf[Float]
		*/
	}

	def shoot() {
		queue.invoke{
			_.shoot(tankId)
		}
	}

	import Tank.LOG

	def moveAngle(theta: Float) = {
		if (dead) {
			LOG.debug("Tried to rotate Tank #" + tankId + " but it is dead")
			(0f, 0)
		} else {
			computeAngle(theta)
		}
	}

	def computeAngle(theta: Float) = {
		val startingAngle = getAngle
		val targetAngle = startingAngle + theta
		val Kp = 1f
		val Kd = 4.5f
		val Ki = 0.0f
		val tol = deg2rad(1)
		val tolv = .1f
		val dt = 300;
		val maxVel = .7854f //constants("tankangvel")

		def getTime = java.util.Calendar.getInstance().getTimeInMillis
		def timeDifference(start: Long, end: Long) = (end - start).asInstanceOf[Int]

		LOG.debug("Starting angle: " + rad2deg(startingAngle) + "; need "+rad2deg(targetAngle))

		def pdController(error0: Float, ierror: Float, time: Long) {
			val angle = getAngle
			val error = targetAngle - angle

			val v = (Kp * error + Ki * ierror * dt + Kd * (error - error0) / dt) / maxVel

			if (abs(error) < tol && abs(v) < tolv) {
				LOG.debug("setting tank#" + tankId + " angular velocity: " + 0)
				setAngularVelocity(0f)
			} else {
				LOG.debug("setting tank#" + tankId + " angular velocity: " + v)
				setAngularVelocity(v.asInstanceOf[Float])
				sleep(dt)
				pdController(error.asInstanceOf[Float], (ierror + error).asInstanceOf[Float], getTime)
			}
		}

		val startTime = getTime
		pdController(0.0f, 0.0f, startTime)
		((getAngle - startingAngle).asInstanceOf[Float], timeDifference(startTime, getTime))
	}
}