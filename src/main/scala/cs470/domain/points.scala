package cs470.domain

class Point(x : Float, y : Float) {
	override def toString = "(" + x + ", " + y + ")"
}

class Polygon(points : Seq[Point]) {

    def convexHull = this

    def inConvexInterior(point : Point) = false
}

// vim: set ts=4 sw=4 et:
