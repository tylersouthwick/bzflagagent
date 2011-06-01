package cs470.domain

/**
 * @author tylers2
 */

trait PolygonFinder extends Occgrid {

	def neighbors(t : (Int, Int)) : Seq[(Int, Int)] = neighbors(t._1, t._2)
	def neighbors(x : Int, y : Int) = Seq((x - 1, y), (x - 1, y + 1), (x, y + 1), (x + 1, y + 1), (x + 1, y), (x + 1, y - 1), (x, y - 1), (x-1, y - 1))

	def polygons : Seq[Polygon] = {
		val polygons = new java.util.LinkedList[Polygon]
		val tmp = Array.ofDim[Occupant.Occupant](width, height)
		for (x <- 0 until width) {
			for (y <- 0 until height) {
				tmp(x)(y) = data(x)(y)
			}
		}

		def corner : Option[(Int, Int)] = {
			for (x <- 0 until width) {
				for (y <- 0 until height) {
					tmp(x)(y) match {
						case Occupant.WALL => {
							return Some((x, y))
						}
						case _ =>
					}
				}
			}
			None
		}

		def findBoxWidth(corner : (Int, Int)) = {
			val (x, y) = corner
			var w = 0
			try {
				while(tmp(x + w)(y) == Occupant.WALL && x + w < width) {
					w = w + 1
				}
			} catch {
				case _ =>
			}
			w
		}

		def findBoxHeight(corner : (Int, Int), width : Int) = {
			var h = 0
			val (x, y) = corner
			try {
				while(tmp(x + width)(y + h) == Occupant.WALL && y + h < height) {
					h = h + 1
				}
			} catch {
				case _ =>
			}
			h
		}

		def buildBox = {
			corner match {
				case Some(corner) => {
					val width = findBoxWidth(corner)
                    val height = (0 to width - 1).map(findBoxHeight(corner, _)).min

					val corners = Seq(corner, (corner._1 + width, corner._2), (corner._1 + width, corner._2 + height), (corner._1, corner._2 + height))
					polygons.add(new Polygon(corners.map(t => getLocation(t._1, t._2))))
					for (x <- corner._1 until corner._1 + width) {
						for (y <- corner._2 until corner._2 + height) {
							tmp(x)(y) = Occupant.NONE
						}
					}
					true
				}
				case None => false
			}
		}

		while (buildBox) {
		}

		import scala.collection.JavaConversions._
		polygons
	}
}

// vim: set ts=4 sw=4 et:
