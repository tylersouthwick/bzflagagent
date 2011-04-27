package cs470.domain

class MyTank(line : String) {
	val splitter = new Parser("mytank", line)

	val id = splitter.getInt
	val callsign = splitter.getString
	val status = splitter.getString
	val shotsAvailable = splitter.getInt
	val timeToReload = splitter.getFloat
	val flag = splitter.getString
	val x = splitter.getInt
	val y = splitter.getInt
	val angle = splitter.getFloat
	val vx = splitter.getFloat
	val xy = splitter.getFloat
	val angvel = splitter.getFloat
}

class OtherTank(line : String) {
    val splitter = new Parser("othertank", line)

    val callsign = splitter.getString
    val color = splitter.getString
    val status = splitter.getString
    val flag = splitter.getString
    val location = new Point(splitter.getFloat, splitter.getFloat)
    val angle = splitter.getFloat
}

class Shot(line : String) {
    val splitter = new Parser("shot", line)
    val location = splitter.getPoint
    val speed = splitter.getVector
}
// vim: set ts=4 sw=4 et:
