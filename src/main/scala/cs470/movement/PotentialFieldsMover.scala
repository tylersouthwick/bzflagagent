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


  val moveWhileTurning = false
  val minimizeTurning = true
  val howClose = 5

  def path: Vector

  val goal: Point

  import RefreshableData.waitForNewData

  private def pdVector = path

  private def pdController(error0: Radian, vector: Vector) {
    LOG.debug("vector: " + vector)
    val targetAngle = vector.angle
    val angle = tank.angle
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
    val v = if (rv > maxVel) 1 else rv / maxVel
    LOG.debug("v: " + v)

    def Kgoal = if (distance > 10) 1 else .1

    //    if (minimizeTurning && abs(PI - abs(error)) < .01) {
    //      LOG.debug("Reversing");
    //      tank.setAngularVelocity(0f)
    //      val speed = {
    //        val m = vector.magnitude
    //        LOG.debug("magnitude: " + m)
    //        val result = m / 30.0
    //        if (result > maxVelocity) {
    //          maxVelocity
    //        } else {
    //          result
    //        }
    //      }
    //      LOG.debug("setting speed: " + -speed)
    //      tank.speed(-speed * Kgoal)
    //      waitForNewData()

    //    } else if (abs(error) < tol && abs(v) < tolv) {
    if (abs(error) < tol && abs(v) < tolv) {
      LOG.debug("Done Turning");
      tank.setAngularVelocity(0f)
      val speed = {1/*
        val m = vector.magnitude
        LOG.debug("magnitude: " + m)
        val result = m / 30.0
        if (result > maxVelocity) {
          maxVelocity
        } else {
          result
        }*/
      }
      LOG.debug("setting speed: " + speed)
      tank.speed(speed * Kgoal)
      waitForNewData()
    } else {
      //Agents.LOG.debug("Setting velocity to " + v)
      tank.setAngularVelocity(v)

      //slow it down to turn
      val turningSpeed = {
		  /*
        if (moveWhileTurning) {
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
          0.0
        }*/
        .5
      }

      LOG.debug("setting speed: " + turningSpeed)
      tank.speed(turningSpeed * Kgoal)
      waitForNewData()
      if (!inRange(vector))
        pdController(error, pdVector)
      stop
    }
  }

  def stop {
    tank.setAngularVelocity(0)
    tank.speed(0)
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

