package cs470.domain

class Base(line : String) {
    private val splitter = new Parser("base", line)
    val color = splitter.getString
    val points = splitter.points
}

class Flag(line : String) {
    private val splitter = new Parser("flag", line)
    val color = splitter.getString
    val possessingTeamColor = splitter.getString
    val location = new Point(splitter.getDouble, splitter.getDouble)
}

class Obstacle(line : String) {
    private val splitter = new Parser("obstacle", line)
    val points = splitter.points
}

// vim: set ts=4 sw=4 et:
