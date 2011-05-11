package cs470.domain

/**
 * @author tylers2
 */

class Occgrid {

	private var step = 0
	private var data : Array[Array[Char]] = null
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
		line.zipWithIndex.foreach { case (obstacle, idx) =>
			rowData(idx) = obstacle
		}
	}

	override def toString = {
		val sb = new StringBuilder
		data.foreach{row =>
			row.foreach {column =>
				sb.append (column) /*{
					if (column) "1" else "0"
				}*/
			}
			sb.append("\n")
		}
		sb.append(width)
		sb.append("x")
		sb.append(height)
		sb.toString()
	}
}