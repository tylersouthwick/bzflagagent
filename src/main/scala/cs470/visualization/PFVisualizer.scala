package cs470.visualization

import cs470.movement.PotentialFieldGenerator
import cs470.domain.Point
import java.io.{FileOutputStream, PrintWriter}

class PFVisualizer(pfgenerator: PotentialFieldGenerator, filename: String, worldsize: Int, samples: Int) {
	import PFVisualizer._
  private val vec_len = 0.75 * worldsize / samples

	LOG.info("Opening file: " + filename)
	private val file = new PrintWriter(new FileOutputStream(filename))

	def visualize() {
		LOG.info("Saving file: " + filename)
		file.close()
		LOG.info("File Saved: " + filename)
	}

  private def plotField() {
    write("plot '-' with vectors head")

    val diff: Int = worldsize / samples
    val samples2: Int = samples / 2

    val grid: Seq[Point] = (-samples2 to samples2).foldLeft(Seq[Point]()) {
      (points, x) =>
        points ++ (-samples2 to samples2).map(y => new Point(x * diff, y * diff))
    }

    grid.foreach {
      point =>
        val vector = pfgenerator.getPFVector(point)
        val endpoint = vector.getArrowHeadPoint(point)
        write(" " + point.x + " " + point.y + " " + endpoint.x + " " + endpoint.y)
    }

    write("e")

//    gpi_point(x, y, vec_x, vec_y):
//    ' ' 'Create the centered gpi data point (4 - tuple) for a position and
//    vector.The vectors are expected to be less than 1 in magnitude,
//    and larger values will be scaled down.' ' '
//    r = (vec_x ** 2 + vec_y ** 2) ** 0.5
//    if r > 1:
//      vec_x /= r
//    vec_y /= r
//    return (x - vec_x * VEC_LEN / 2, y - vec_y * VEC_LEN / 2,
//      vec_x * VEC_LEN, vec_y * VEC_LEN)
    //
    //    for x, y in points:
    //      (f_x, f_y) = function(x, y)
    //    plotvalues = gpi_point(x, y, f_x, f_y)
    //    if plotvalues is not None:
    //      x1, y1, x2, y2 = plotvalues
    //    s += '% s % s % s % s \ n ' %(x1, y1, x2, y2)
    //    s += 'e\ n '
  }

  private def setGnuPlotHeader(minimum: Int, maximum: Int) {
    write("set xrange [%d,%d]".format(minimum, maximum))
    write("set yrange [%d,%d]".format(minimum, maximum))
    write("unset key")
    write("set size square")
    write("set title 'Potential Fields'")
  }

	def write(s : String) {
		file.println(s)
	}
}

object PFVisualizer {
  val LOG = org.apache.log4j.Logger.getLogger("cs470.visualizer.pf")
}