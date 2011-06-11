package cs470.utils

import java.lang.Math._
import org.apache.log4j.Logger.getLogger
import cs470.bzrc.{DataStore, RefreshableData, Tank}
import cs470.domain._
import cs470.domain.Constants._

/**
 * @author tylers2
 */

trait PDController {
	def move()
}

class TurningPDController(targetAngle : Angle, tank: Tank) extends PDController {

	var Kp = 1
	var Kd = .1
	var tol = Degree(4).radian
	var tolv = .1
	var maxVel = .7854

	private def pdController(error0: Radian) {
		val uncorrectedError = (targetAngle - tank.angle).radian

		//println("angle: " + angle.degree)
		//println("targetAngle: " + targetAngle.degree)
		//Correct for right turns
		val moddedError = uncorrectedError % (2 * PI)
		val errorM = moddedError % PI
		val error = new Radian(if (errorM == moddedError) errorM else -errorM)
		//println("error: " + error.degree)

		val rv = (Kp * error + Kd * (error - error0));
		val v = if (rv > maxVel) 1 else rv / maxVel

		if (abs(error) < tol && abs(v) < tolv) {
			tank.setAngularVelocity(0f)
		} else {
			//Agents.LOG.debug("Setting velocity to " + v)
			tank.setAngularVelocity(v)
			RefreshableData.waitForNewData()
			pdController(error)
		}
	}

	final def move() {
		pdController(Radian(0))
	}
}

object MovingPDController {
	val LOG = getLogger(classOf[MovingPDController])
}

abstract class MovingPDController(goal : Point, tank: Tank, store : DataStore) extends PDController {
	import cs470.utils.MovingPDController._
	private val constants = store.constants
	var Kp = 1
	var Kd = 4.5
	var tol = Degree(5).radian
	var tolv = .1
	var maxVel: Double = constants("tankangvel")
	var maxMagnitude = 100.0
	var maxVelocity = Properties("maxVelocity", 1)
	var angularTolerance = Degree(90).radian

	var howClose = 5

	def direction : Vector
	private def path = direction

	def getTurningSpeed(angle: Double): Double = {
		if (angle > angularTolerance) {
			0.0
		} else if (angle > angularTolerance / 2) {
			0.2
		} else if (angle > angularTolerance / 4) {
			0.4
		} else {
			1.0
		}
	}

	import RefreshableData.waitForNewData

	private def pdVector = path

	private def pdController(error0: Radian, vector: Vector) {
		LOG.debug("vector: " + vector)
		val targetAngle = vector.angle
		val angle = tank.angle
		LOG.debug("Tank: " + tank.callsign)
		LOG.debug("targetAngle: " + targetAngle.degree)
		LOG.debug("angle: " + angle.degree)
		val uncorrectedError = (targetAngle - angle).radian

		//Correct for right turns
		val moddedError = uncorrectedError % (2 * PI)
		val errorM = moddedError % PI
		val error = new Radian(if (errorM == moddedError) errorM else -errorM)

		LOG.debug("error: " + error.degree)
		val rv = (Kp * error + Kd * (error - error0) / 200);
		LOG.debug("rv: " + rv)
		val v = rv / maxVel
		LOG.debug("v: " + v)

		val (finalSpeed: Double, finalAngVel: Double) = if (abs(error) < tol && abs(v) < tolv) {
			val speed = {
				val m = vector.magnitude
				LOG.debug("magnitude: " + m)
				val result = m / 30.0
				if (result > maxVelocity) {
					maxVelocity
				} else {
					result
				}
			}
			waitForNewData()
			(speed, 0.0)
		} else {
			val turningSpeed = getTurningSpeed(abs(error))

			(turningSpeed, v)
		}

		LOG.debug("Setting " + tank.callsign + " to speed: " + finalSpeed + " and angvel: " + finalAngVel)
		tank.setSpeed(finalSpeed)
		tank.setAngularVelocity(finalAngVel)

		waitForNewData()

		if (!inRange(vector)) {
			pdController(error, pdVector)
		} else {
			stop()
		}
	}

	private def stop() {
		tank.setAngularVelocity(0)
		tank.setSpeed(0)
	}

	protected def distance = goal.distance(tank.location)

	def inRange(vector: Vector) = distance > howClose

	private val ZERO = Radian(0)

	final def move() {
		LOG.debug("Moving " + tank.callsign)
		if (!inRange(pdVector)) {
			pdController(ZERO, pdVector)
		}
		LOG.debug("Ending move for " + tank.callsign)
	}

}
