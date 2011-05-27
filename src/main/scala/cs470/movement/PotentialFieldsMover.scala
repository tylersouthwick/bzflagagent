package cs470.movement

import cs470.domain._
import Constants._
import cs470.utils._
import Angle._
import java.lang.Math._
import cs470.bzrc._

object PotentialFieldsMover {
	val LOG = org.apache.log4j.Logger.getLogger(classOf[PotentialFieldsMover])
}

abstract class PotentialFieldsMover(store: DataStore) {

	import PotentialFieldsMover._

	val constants = store.constants
	val Kp = 1
	val Kd = 4.5
	val tol = degree(5).radian
	val tolv = .1
	val maxVel: Double = constants("tankangvel")
	val worldsize: Int = constants("worldsize")
	val maxMagnitude = 100.0
	val maxVelocity = Properties("maxVelocity", 1)
	val team = constants("team")
	val angularTolerance = degree(90).radian
	val tank: Tank

	val constantSpeed: Double = 0.0
	val moveWhileTurning = false
	val minimizeTurning = true
	val howClose = 5

	def path: Vector

	def goal: Point

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

		def Kgoal = 1 //if (distance > 10) 1 else .1

		//    if (minimizeTurning && abs(PI - abs(error)) < .01) {
		//      LOG.debug("Reversing");
		//      tank.setAngularVelocity(0f)
		//      val setSpeed = {
		//        val m = vector.magnitude
		//        LOG.debug("magnitude: " + m)
		//        val result = m / 30.0
		//        if (result > maxVelocity) {
		//          maxVelocity
		//        } else {
		//          result
		//        }
		//      }
		//      LOG.debug("setting setSpeed: " + -setSpeed)
		//      tank.setSpeed(-setSpeed * Kgoal)
		//      waitForNewData()

		//    } else if (abs(error) < tol && abs(v) < tolv) {
		val (finalSpeed: Double, finalAngVel: Double) = if (abs(error) < tol && abs(v) < tolv) {
			val speed = {
				1.0 /*
        val m = vector.magnitude
        LOG.debug("magnitude: " + m)
        val result = m / 30.0
        if (result > maxVelocity) {
          maxVelocity
        } else {
          result
        }*/
			}
			waitForNewData()
			(speed, 0.0)
		} else {
			val turningSpeed = {
				if (moveWhileTurning) {
					//slow it down to turn
					if (abs(error) > angularTolerance) {
						0.0
					} else if (abs(error) > angularTolerance / 2) {
						0.2
					} else if (abs(error) > angularTolerance / 4) {
						0.4
					} else {
						1.0
					}
				} else {
					constantSpeed
				}
			}
			(turningSpeed, v)
		}
		LOG.debug("Setting " + tank.callsign + " to speed: " + finalSpeed + " and angvel: " + finalAngVel)
		tank.setSpeed(finalSpeed)
		tank.setAngularVelocity(finalAngVel)

		waitForNewData()
		//		LOG.debug("Speed is: " + tank.speed)
		if (!inRange(vector))
			pdController(error, pdVector)
		stop
	}

	def stop {
		tank.setAngularVelocity(0)
		tank.setSpeed(0)
	}

	private val origin = new Point(0, 0)

	private def distance = goal.distance(tank.location)

	def inRange(vector: Vector) = {
		distance > howClose
	}

	private def doPdController(vector: Vector) = {
		if (inRange(vector)) {
			pdController(radian(0), vector)
			true
		} else false
	}

	def moveAlongPotentialField() {
		LOG.info("Moving " + tank.callsign)
		while (doPdController(pdVector)) {
		}
		LOG.info("Ending move for " + tank.callsign)
	}

}

