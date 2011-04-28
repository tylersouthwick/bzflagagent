package cs470.domain

trait MyTank {

	def id : Int
	def callsign : String
	def status : String
	def shotsAvailable : Int
	def timeToReload : Float
	def flag : String
	def location : Point
	def angle : Float
	def vx : Float
	def xy : Float
	def angvel : Float

	override def toString = callsign + "@" + location
}

class MyTankImpl(line : String) extends MyTank {
	private val splitter = new Parser("mytank", line)

	val id = splitter.getInt
	val callsign = splitter.getString
	val status = splitter.getString
	val shotsAvailable = splitter.getInt
	val timeToReload = splitter.getFloat
	val flag = splitter.getString
	val location = splitter.getPoint
	val angle = splitter.getFloat
	val vx = splitter.getFloat
	val xy = splitter.getFloat
	val angvel = splitter.getFloat

	override def toString = callsign + "@" + location
}

class OtherTank(line : String) {
    private val splitter = new Parser("othertank", line)

    val callsign = splitter.getString
    val color = splitter.getString
    val status = splitter.getString
    val flag = splitter.getString
    val location = new Point(splitter.getFloat, splitter.getFloat)
    val angle = splitter.getFloat

	override def toString = callsign + "[" + color + "] @" + location
}

class Shot(line : String) {
    private val splitter = new Parser("shot", line)
    val location = splitter.getPoint
    val speed = splitter.getVector
}
// vim: set ts=4 sw=4 et:
