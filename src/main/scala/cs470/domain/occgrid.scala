package cs470.domain

/**
 * @author tylers2
 */

class Occgrid extends Traversable[Array[Occupent.Occupent]] {

	private var step = 0
	private var data : Array[Array[Occupent.Occupent]] = null
	private var row = 0
	private var width = 0
	private var height = 0

	def read(line : String) {
		step match {
			case 0 => setOffset(line)
			case 1 => createMatrix(line)
			case _ => addToMatrix(line)
		}
		step = step + 1
	}

	private def setOffset(line : String) {
		val tokens = line.split("\\s")
		val at = tokens(0)
		val dim = tokens(1).split(",")
		val x = Integer.parseInt(dim(0))
		val y = Integer.parseInt(dim(1))
		new Point(x, y)
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
			case '1' => Occupent.WALL
			case '0' => Occupent.NONE
		}.zipWithIndex.foreach { case (obstacle, idx) =>
			rowData(idx) = obstacle
		}
	}


	def foreach[U](f: (Array[Occupent.Occupent]) => U) {
		data.foreach(f)
	}

	override def toString = {
		val sb = new StringBuilder
		sb.append("Occgrid [")
		sb.append(width)
		sb.append("x")
		sb.append(height)
		sb.append("]")
		sb.toString()
	}
}

object Occupent extends Enumeration {
	type Occupent = Value

	val NONE, WALL, ENEMY, TANK = Value
}