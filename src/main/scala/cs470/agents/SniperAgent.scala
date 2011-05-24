package cs470.agents

import cs470.domain.Constants._
import cs470.domain.Point
import cs470.utils._
import Angle._
import cs470.bzrc._
import cs470.movement.pfFindFlag
import cs470.visualization.PFVisualizer

class SniperAgent(tank : Tank, store : DataStore) extends MultiAgentBase(tank, store) with Threading {
	val offset = 50
	val prePositionPoint = opponentFlag - new Point(shotrange - offset, shotrange - offset)

	def enemies = store.enemies.filter(_.color == "green").filter(_.status == "alive")

	override def apply() {
		loop {
			super.apply()

			tank.speed(0)

			enemies.foreach(killEnemy)

			gotoFlag()
		}
	}

	import cs470.domain.Vector
	def killEnemy(enemy : Enemy) {
		def vector = new Vector(enemy.location - tank.location)
		while (enemy.status == "alive") {
			//println("Trying to kill " + enemy.callsign + " - angle was " + angle.degree)
			val angle = vector.angle
			println("shooting [" + enemy.callsign + "] moving angle: " + angle.degree)
			tank.moveToAngle(angle)
			tank.shoot()
			RefreshableData.waitForNewData()
		}
	}

	val Kp = 1
	val Kd = 4.5
	val tol = degree(2).radian
	val tolv = .1
	val maxVel: Double = constants("tankangvel")
	val worldsize: Int = constants("worldsize")
	val offsetVector = new Vector(new Point(worldsize / 2, worldsize / 2))
	val maxMagnitude = 100.0
	val maxVelocity = 1
	val team = constants("team")
	val turningSpeed = 0.6
	import java.lang.Math._
	def gotoFlag() {
		def move(pdVector : => Vector) {
			//	tank.speed(vector.magnitude / maxMagnitude)
			//val (angle, time) = tank.moveAngle(vector.angle)

			def pdController(error0: Radian, vector: Vector) {
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
					RefreshableData.waitForNewData()
				} else {
					//Agents.LOG.debug("Setting velocity to " + v)
					tank.setAngularVelocity(v)
					//slow it down to turn
					LOG.debug("setting speed: " + turningSpeed)
					tank.speed(turningSpeed)
					RefreshableData.waitForNewData()
					pdController(error, pdVector)
				}
			}

			pdController(radian(0), pdVector)
		}
		val flagFinders = flags filter (_.color == "green") map {flag => new {
			val finder = new pfFindFlag(store, flag.color)
			def path = finder.getPathVector(tank.location)
		}}
		def findFlag() {
			println("findFlag: " + flagFinders)
			flagFinders.foreach(finder => move(finder.path))
		}
		LOG.debug("flag: " + tank.flag)
		loop {
			tank.flag match {
				case None => findFlag()
				case Some(flag) => {
					returnHome()
					return
				}
			}
		}
	}
}

