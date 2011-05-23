package cs470.movement

import cs470.domain._
import Constants._
import cs470.movement._
import cs470.utils._
import Angle._
import java.lang.Math._
import cs470.bzrc._
import cs470.visualization.PFVisualizer
import cs470.agents.PotentialFieldAgent

abstract class PotentialFieldsMover(store : DataStore) {

	import PotentialFieldAgent._

	val constants = store.constants
	val Kp = 1
	val Kd = 4.5
	val tol = degree(2).radian
	val tolv = .1
	val maxVel: Double = constants("tankangvel")
	val worldsize: Int = constants("worldsize")
	val maxMagnitude = 100.0
	val maxVelocity = Properties("maxVelocity",.3)
	val team = constants("team")
	val turningSpeed = Properties("angspeed", 0.4)
	val tank : Tank

	def path : Vector

	import RefreshableData.waitForNewData

	private def pdVector = path
	private def pdController(error0: Radian, vector: Vector) {
		val targetAngle = vector.angle
		val angle = tank.angle
		LOG.debug("targetAngle: " + targetAngle.degree)
		LOG.debug("angle: " + angle.degree)
		val error = targetAngle - angle

		LOG.debug("error: " + error.degree)
		val rv = (Kp * error + Kd * (error - error0) / 200);
		LOG.debug("rv: " + rv)
		val v = if (rv > maxVel) 1 else rv / maxVel
		LOG.debug("v: " + v)

		if (abs(error) < tol && abs(v) < tolv) {
			LOG.debug("Done Turning");
			tank.setAngularVelocity(0f)
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
			LOG.debug("setting speed: " + speed)
			tank.speed(speed)
			waitForNewData()
		} else {
			//Agents.LOG.debug("Setting velocity to " + v)
			tank.setAngularVelocity(v)
			//slow it down to turn
			LOG.debug("setting speed: " + turningSpeed)
			tank.speed(turningSpeed)
			waitForNewData()
			pdController(error, pdVector)
		}
	}

	def moveAlongPotentialField() {
		pdController(radian(0), pdVector)
	}

}

