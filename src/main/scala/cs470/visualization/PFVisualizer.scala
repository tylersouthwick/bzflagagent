package cs470.visualization

import cs470.movement.PotentialFieldGenerator
import cs470.domain.Point
import cs470.domain.Vector
import java.lang.StringBuilder

class PFVisualizer(pfgenerator: PotentialFieldGenerator, filename: String, worldsize: Int, samples: Int) {
  private val vec_len = 0.75 * worldsize / samples

  import PFVisualizer._

  def visualize {
    s = new StringBuilder

    setGnuPlotHeader(s,)
  }

  private def plotField(s:StringBuilder) = {
    s.append("plot '-' with vectors head\n")

    val diff: Int = worldsize / samples
    val samples2: Int = samples / 2

    val grid: Seq[Point] = (-samples2 to samples2).foldLeft(Seq[Point]()) {
      (points, x) =>
        points :: (-samples2 to samples2).map(new Point(x * diff, _ * diff))
    }

    grid.foreach {
      point =>
        val vector = pfgenerator.getPFVector(point)
        val endpoint = vector.getArrowHeadPoint(point)
        s.append(" " + point.x + " " + point.y + " " + endpoint.x + " " + endpoint.y + "\n")
    }

    s.append("e\n")

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

    s
  }

  private def setGnuPlotHeader(s: StringBuilder, minimum: Int, maximum: Int) {
    s.append("set xrange [%d,%d]\n".format(minimum, maximum))
    s.append("set yrange [%d,%d]\n".format(minimum, maximum))
    s.append("unset key\n")
    s.append("set size square\n")
    s.append("set title 'Potential Fields'\n")
  }
}

object PFVisualizer {
  val LOG = org.apache.log4j.Logger.getLogger("cs470.visualizer.pf")
}