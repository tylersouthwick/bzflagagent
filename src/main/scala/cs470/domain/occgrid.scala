package cs470.domain

import collection.mutable.{HashMap, LinkedList}

/**
 * @author tylers2
 */

class Occgrid extends Traversable[Array[Occupant.Occupant]] {

	private var step = 0
	var data : Array[Array[Occupant.Occupant]] = null
	private var row = 0
	var width = 0
	var height = 0
	var offset = (0, 0)

	def read(line : String) {
		step match {
			case 0 => setOffset(line)
			case 1 => createMatrix(line)
			case _ => addToMatrix(line)
		}
		step = step + 1
	}

	def addEnemies(points: Traversable[Point]) {
		addOccupants(points, Occupant.ENEMY)
	}

	def addOccupants(points : Traversable[Point], occupent : Occupant.Occupant) {
		points map(convert) foreach {case (x, y) =>
			data(x)(y) = occupent
		}
	}

	private def setOffset(line : String) {
		val tokens = line.split("\\s")
		val at = tokens(0)
		val dim = tokens(1).split(",")
		val x = Integer.parseInt(dim(0))
		val y = Integer.parseInt(dim(1))
		offset = (x, y)
	}

	private def createMatrix(line : String) {
		val tokens = line.split("\\s")
		val at = tokens(0)
		val dim = tokens(1).split("x")
		width = Integer.parseInt(dim(0))
		height = Integer.parseInt(dim(1))

		data = Array.ofDim(width, height)
	}

	private def addToMatrix(line : String) {
		val rowData = data(row)
		row = row + 1
		line.map{
			case '1' => Occupant.WALL
			case '0' => Occupant.NONE
		}.zipWithIndex.foreach { case (obstacle, idx) =>
			rowData(idx) = obstacle
		}
	}

	def foreach[U](f: (Array[Occupant.Occupant]) => U) {
		data.foreach(f)
	}

	def convert(location : Point) = (location.x.intValue - offset._1, location.y.intValue - offset._2)

	override def toString() = {
		val sb = new StringBuilder
		sb.append("Occgrid [")
		sb.append(width)
		sb.append("x")
		sb.append(height)
		sb.append("]")
		sb.toString()
	}
}

object Occupant extends Enumeration {
	type Occupant = Value

	val NONE, WALL, ENEMY = Value
}

