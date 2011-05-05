package cs470.agents

import cs470.domain._
import Constants._
import cs470.movement._
import cs470.utils._
import Angle._
import java.lang.Math._
import cs470.bzrc._
import cs470.visualization.Visualizer

class PotentialFieldAgent(host: String, port: Int) extends Agent(host, port) with Threading {

	import PotentialFieldAgent._

	def run() {
		LOG.info("Running potential field agent")
		val tanks = myTanks
		//pick a tank
		tanks.filter(_.tankId == 1).foreach {
			tank =>
				moveAlongPotentialField(tank)
		}

		/*
		val pfgen = new pfReturnToGoal(store,"blue")
		val vis = new Visualizer(pfgen,"pf.gpi",obstacles, convertInt(constants("worldsize")),25)
		*/
	}

	val Kp = 1
	val Kd = 4.5
	val tol = degree(2).radian
	val tolv = .1
	val maxVel: Double = constants("tankangvel")
	val worldsize: Int = constants("worldsize")
	val offsetVector = new Vector(new Point(worldsize / 2, worldsize / 2))
	val maxMagnitude = 100.0
	val maxVelocity = 1.0
	val team = constants("team")

	def moveAlongPotentialField(tank: Tank) {
		val flagFinders = flags map {
			flag =>
				new {
					private val findFlag = new pfFindFlag(store, flag.color)

					def path = {
						val vis = new Visualizer(findFlag, "pf.gpi", obstacles, worldsize, 25)
						findFlag.getPathVector(tank.location)
					}

					val color = flag.color
					def possessingTeamColor = flag.possessingTeamColor
				}
		}
		val homeFinder = new {
			val finder = new pfReturnToGoal(store, team)
			def path = finder.getPathVector(tank.location)
		}

		val finder = flagFinders(1)
		actor {
			loop {
				def waitForNewData() {
					RefreshableData.waitForNewData()
					tank.shoot()
				}
				tank.shoot();

				def move(pdVector : Vector) {
					//	tank.speed(vector.magnitude / maxMagnitude)
					//val (angle, time) = tank.moveAngle(vector.angle)

					def pdController(error0: Radian, vector: Vector) {
						val targetAngle = vector.angle
						val angle = tank.angle
						LOG.debug("targetAngle: " + targetAngle.degree)
						LOG.debug("angle: " + angle.degree)
						val error = targetAngle - angle

						LOG.debug("error: " + error)
						val rv = (Kp * error + Kd * (error - error0) / 200);
						LOG.debug("rv: " + rv)
						val v = if (rv > maxVel) 1 else rv / maxVel
						LOG.debug("v: " + v)

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

						if (abs(error) < tol && abs(v) < tolv) {
							LOG.debug("Done Turning");
							tank.setAngularVelocity(0f)
							waitForNewData()
						} else {
							//Agents.LOG.debug("Setting velocity to " + v)
							tank.setAngularVelocity(v)
							waitForNewData()
							pdController(error, pdVector)
						}
					}

					pdController(radian(0), pdVector)
				}
				def findFlag() {
					LOG.debug("going to flag")
					move(finder.path)
				}
				def returnHome() {
					LOG.debug("going home")
					move(homeFinder.path)
				}

				LOG.debug("flag: " + tank.flag)
				tank.flag match {
					case None => findFlag()
					case flag : Some[String] => returnHome()
				}
			}
		}
	}

}

object PotentialFieldAgent extends AgentCreator {
	val LOG = org.apache.log4j.Logger.getLogger("cs470.agents.PotentialFieldAgent")

	def name = "pf"

	def create(host: String, port: Int) = new PotentialFieldAgent(host, port)
}
