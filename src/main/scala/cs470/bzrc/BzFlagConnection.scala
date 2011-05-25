package cs470.bzrc

import java.io._
import java.net._
import java.util._
import java.lang.{Integer => JInteger}
import scala.collection.JavaConversions._
import cs470.domain._

object BZRC {
	val VERSION = 1
	val LOG = org.apache.log4j.Logger.getLogger(classOf[BzFlagConnection])
}

class BzFlagConnection(host : String, port : Int) {
	private val socket = new Socket(host, port)
    println("nodelay: " + socket.getTcpNoDelay)
    socket.setTcpNoDelay(false)

	if (!socket.isConnected) throw new UnableToConnectException

	private val in = new BufferedReader(new InputStreamReader(socket.getInputStream))
	private val out = new PrintWriter(socket.getOutputStream, true)

	import BZRC._

	//read welcome line
	{
		val line = receive
		if (line == null) throw new InvalidHandshakeException
		LOG.debug("received handshake :: " + line)
		val version = Integer.parseInt(line.split("\\s").apply(1))
		if (version != VERSION) throw new InvalidBZRCVersionException
		out.println("agent " + VERSION)
	}

	private def send(s : String) {
		LOG.debug("sending " + s)
		out.println(s)
		LOG.debug("sent")
		ack
	}
	private def readLine = in.readLine
	private def receive = readLine
	private def ack = LOG.debug("ack: " + readLine)
	private def status = LOG.debug("status: " + readLine)

	private def receive(callback : (String) => Unit) {
		val begin = readLine
		if (!"begin".equals(begin)) throw new InvalidBlockException(begin)

		var line = readLine
		while (!"end".equals(line)) {
			callback(line)
			line = readLine
		}
	}

	private def receiveItems[T](command : String, callback : (String) => T) : Seq[T] = {
        send(command)
		val begin = readLine
		if (!"begin".equals(begin)) throw new InvalidBlockException(begin)

		var line = readLine
		var list = new java.util.LinkedList[T]
		while (!"end".equals(line)) {
			list.add(callback(line))
			line = readLine
		}
		list
	}

	def shoot(agent : Int) {
		send("shoot " + agent)
		status
	}

	private def movement(action : String, agent : Int, speed : Double) {
		if (speed == Double.NaN) {
			LOG.warn("Trying to set " + action + " to NaN")
		} else {
			send(action + " " + agent + " " + speed)
			status
		}
	}

	def speed(agent : Int, speed : Double) = movement("speed", agent, speed)
	def angvel(agent : Int, speed : Double) = movement("angvel", agent, speed)
	def accelx(agent : Int, speed : Double) = movement("accelx", agent, speed)
	def accely(agent : Int, speed : Double) = movement("accely", agent, speed)

	def teams = receiveItems("teams", new Team(_))
	def obstacles = receiveItems("obstacles", new Obstacle(_))
	def bases = receiveItems("bases", new Base(_))
	def flags = receiveItems("flags", new Flag(_))
	def shots = receiveItems("shots", new Shot(_))
	def mytanks = receiveItems("mytanks", new MyTank(_))
	def othertanks = receiveItems("othertanks", new OtherTank(_))

    def constants = new Constants(receiveItems("constants", new Constant(_)))

	def occgrid(agent : Int) = {
		send("occgrid " + agent)
		val occgrid = new OccgridCommand
		receive (occgrid.read)
		occgrid
	}
}

class BZRCException(msg : String) extends Exception(msg)
class UnableToConnectException extends BZRCException("Unable to connect")
class InvalidHandshakeException extends BZRCException("Invalid Handshake")
class InvalidBZRCVersionException extends BZRCException("Invalid BZRC Version")
class InvalidBlockException(line : String) extends BZRCException("expected 'begin' found '" + line +"'")

// vim: set ts=4 sw=4 et:
