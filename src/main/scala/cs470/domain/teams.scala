package cs470.domain

class Team(line : String) {
    val splitter = new Parser("team", line)
    val color = splitter.getString
    val playerCount = splitter.getInt
}

// vim: set ts=4 sw=4 et:
