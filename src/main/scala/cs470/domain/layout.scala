package cs470.domain

class Base(line: String) {
  private val splitter = new Parser("base", line)
  val color = splitter.getString
  val points = new Polygon(splitter.points)
}

class Flag(line: String) {
  private val splitter = new Parser("flag", line)
  val color = splitter.getString
  val possessingTeamColor = splitter.getString
  val location = new Point(splitter.getDouble, splitter.getDouble)
}

class Obstacle(points : Seq[Point]) extends Polygon(points) {

  def this(line : String) {
    this(new Parser("obstacle", line).points)
  }
}

// vim: set ts=4 sw=4 et:
