package cs470.domain

import collection.mutable.ListMap

class Constant(line : String) {
    private val splitter = new Parser("constant", line)
    val name = splitter.getString
    val value = splitter.getString
}

class Constants(constants : Seq[Constant]) extends ListMap[String, String] {

	constants.foreach {constant =>
		this += constant.name -> constant.value
	}

	def getAsInt(name : String) = Integer.parseInt(this(name))

	def getAsDouble(name : String) = java.lang.Double.parseDouble(this(name))
}

// vim: set ts=4 sw=4 et:
