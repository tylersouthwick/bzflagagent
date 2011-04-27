package cs470.domain

class Base(line : String) {
    val splitter = new Parser("base", line)
    val color = splitter.getString
    val points = splitter.points
}

class Flag(line : String) {
    val splitter = new Parser("flag", line)
    val color = splitter.getString
    val possessingTeamColor = splitter.getString
    val location = new Point(splitter.getFloat, splitter.getFloat)
}

class Obstacle(line : String) {
    val splitter = new Parser("obstacle", line)
    val points = splitter.points
}

class Point(x : Float, y : Float)

// vim: set ts=4 sw=4 et: