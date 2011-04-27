package cs470.domain

import scala.collection.JavaConversions._

object Parser {
    val LOG = org.apache.log4j.Logger.getLogger(classOf[Parser])
    val debug = LOG.isDebugEnabled
}

class Parser(prefix : String, line : String) {
    import Parser._
    if (debug) LOG.debug(prefix + " :: " + line)

	private val tokens = line.split("\\s")
	private var i = 0
	private def index = {
		val old = i
		i = i + 1
		old
	}
	def getString = tokens.apply(index)
	def getInt = Integer.parseInt(getString)
	def getFloat = java.lang.Float.parseFloat(getString)
    def getPoint = new Point(getFloat, getFloat)
    def getVector = getPoint

    def points = {
        val list = new java.util.LinkedList[Point]
        while (hasMore) {
            list.add(new Point(getFloat, getFloat))
        }
        new Polygon(list)
    }

    def hasMore = i < tokens.length

    getString
}

// vim: set ts=4 sw=4 et:
