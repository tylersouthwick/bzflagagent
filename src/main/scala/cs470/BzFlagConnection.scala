package cs470

import java.io._
import java.net._
import java.util._
import java.lang.{Integer => JInteger}
import scala.collection.JavaConversions._

object BZRC {
	val VERSION = 1
	val LOG = org.apache.log4j.Logger.getLogger(classOf[BzFlagConnection])
}

class BzFlagConnection(host : String, port : Int) {
	val socket = new Socket(host, port)

	if (!socket.isConnected) throw new UnableToConnectException

	val in = new BufferedReader(new InputStreamReader(socket.getInputStream))
	val out = new PrintWriter(socket.getOutputStream, true)

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
		out.println(s)
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

	private def receiveItems[T](callback : (String) => T) : Seq[T] = {
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

	private def movement(action : String, agent : Int, speed : Float) {
		send(action + " " + agent + " " + speed)
		status
	}

	def speed(agent : Int, speed : Float) = movement("speed", agent, speed)
	def angvel(agent : Int, speed : Float) = movement("angvel", agent, speed)
	def accelx(agent : Int, speed : Float) = movement("accelx", agent, speed)
	def accely(agent : Int, speed : Float) = movement("accely", agent, speed)

	def teams {
		send("teams")
		receive { line =>
			LOG.debug("teams :: " + line)
		}
	}

	def obstacles {
		send("obstacles")
		receive { line =>
			LOG.debug("obstacles :: " + line)
		}
	}

	def bases {
		send("bases")
		receive { line =>
			LOG.debug("bases :: " + line)
		}
	}

	def flags {
		send("flags")
		receive { line =>
			LOG.debug("flags :: " + line)
		}
	}

	def shots {
		send("shots")
		receive { line =>
			LOG.debug("shots :: " + line)
		}
	}

	def mytanks = {
		send("mytanks")
		receiveItems { line =>
			LOG.debug("mytanks :: " + line)
			new MyTank(line)
		}
	}

	def othertanks {
		send("othertanks")
		receive { line =>
			LOG.debug("othertanks :: " + line)
		}
	}

	def constants = {
		send("constants")
		receiveItems { line =>
			LOG.debug("Received Line: " + line)
			val tokens = line.split("\\s")
			val name = tokens.apply(1)
			val value = tokens.apply(2)
			new Constant(name, value)
		}
	}

	def occgrid(agent : Int) {
		send("occgrid " + agent)
		receive { line =>
			LOG.debug("occgrid :: " + line)
		}
	}
}

class Splitter(line : String) {
	private val tokens = line.split("\\s")
	private var i = 0
	private def index = {
		val old = i
		i = i + 1
		old
	}
	def get = tokens.apply(index)
	def getInt = Integer.parseInt(get)
	def getFloat = java.lang.Float.parseFloat(get)
}
class Constant(key : String, value : String)
class MyTank(line : String) {
	val splitter = new Splitter(line)
	def get = splitter.get
	def getInt = splitter.getInt
	def getFloat = splitter.getFloat

	get
	val id = getInt
	val callsign = get
	val status = get
	val shotsAvailable = getInt
	val timeToReload = getFloat
	val flag = get
	val x = getInt
	val y = getInt
	val angle = getFloat
	val vx = getFloat
	val xy = getFloat
	val angvel = getFloat
}
class BZRCException(msg : String) extends Exception(msg)
class UnableToConnectException extends BZRCException("Unable to connect")
class InvalidHandshakeException extends BZRCException("Invalid Handshake")
class InvalidBZRCVersionException extends BZRCException("Invalid BZRC Version")
class InvalidBlockException(line : String) extends BZRCException("expected 'begin' found '" + line +"'")

// vim: set ts=4 sw=4 et:
