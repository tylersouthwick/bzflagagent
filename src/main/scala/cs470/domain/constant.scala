package cs470.domain

import collection.mutable.ListMap

class Constant(line : String) {
    private val splitter = new Parser("constant", line)
    val name = splitter.getString
    val value = splitter.getString

	override def toString = name + ": " + value
}

object Constants {
  implicit def convertDouble(s : String) = java.lang.Double.parseDouble(s)
  implicit def convertInt(s : String) = Integer.parseInt(s)

	private[Constants] val LOG = org.apache.log4j.Logger.getLogger(classOf[Constants])
}

class Constants(constants : Seq[Constant]) extends (String => String) {
	val map = new ListMap[String, String]
	import Constants._

	LOG.debug("Constants:")
	constants.foreach {constant =>
		LOG.debug("\t" + constant)
		map += constant.name -> constant.value
	}

	def apply(name: String) = map(name)
}

// vim: set ts=4 sw=4 et:
