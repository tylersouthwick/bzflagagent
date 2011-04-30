package cs470.domain

class MyTank(line : String) {
	private val splitter = new Parser("mytank", line)

	val id = splitter.getInt
	val callsign = splitter.getString
	val status = splitter.getString
	val shotsAvailable = splitter.getInt
	val timeToReload = splitter.getDouble
	val flag = splitter.getString
	val location = splitter.getPoint
	val angle = splitter.getDouble
	val vx = splitter.getDouble
	val xy = splitter.getDouble
	val angvel = splitter.getDouble

	override def toString = callsign + "@" + location
}

class OtherTank(line : String) {
    private val splitter = new Parser("othertank", line)

    val callsign = splitter.getString
    val color = splitter.getString
    val status = splitter.getString
    val flag = splitter.getString
    val location = new Point(splitter.getDouble, splitter.getDouble)
    val angle = splitter.getDouble

	override def toString = callsign + "[" + color + "] @" + location
}

class Shot(line : String) {
    private val splitter = new Parser("shot", line)
    val location = splitter.getPoint
    val speed = splitter.getVector
}
// vim: set ts=4 sw=4 et:
