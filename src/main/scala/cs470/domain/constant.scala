package cs470.domain

class Constant(line : String) {
    val splitter = new Parser("constant", line)
    val name = splitter.getString
    val value = splitter.getString
}

// vim: set ts=4 sw=4 et:
