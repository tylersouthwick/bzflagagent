package cs470.visualization

import cs470.movement.PotentialFieldGenerator
import cs470.domain.Point
import java.io.{FileOutputStream, PrintWriter}

class PFVisualizer(pfgenerator: PotentialFieldGenerator, filename: String, worldsize: Int, samples: Int) {

  import PFVisualizer._

  private val vec_len = 0.75 * worldsize / samples

  LOG.debug("Opening file for visualization for potential fields to: " + filename)
  private val file = new PrintWriter(new FileOutputStream(filename))

  setGnuPlotHeader()
  plotField()

  LOG.debug("Saving PF visualization to file: " + filename)
  file.close()
  LOG.info("Visualization for potential fields saved to: " + filename)

  private def plotField() {
    write("plot '-' with vectors head")

    val diff: Int = worldsize / samples
    val samples2: Int = samples / 2

    val grid: Seq[Point] = (-samples2 to samples2).foldLeft(Seq[Point]()) {
      (points, x) =>
        points ++ (-samples2 to samples2).map(
          y => new Point(x * diff, y * diff)
        )
    }


    grid.foreach {
      point =>
        val vector = pfgenerator.getPFVector(point)
        val endpoint = vector
        write(" " + point.x + " " + point.y + " " + endpoint.x + " " + endpoint.y)
    }

    write("e")

  }

  private def setGnuPlotHeader() {
    val size2 = worldsize / 2
    write("set xrange [%d:%d]".format(-size2, size2))
    write("set yrange [%d:%d]".format(-size2, size2))
    write("unset key")
    write("set size square")
    write("set title 'Potential Fields'")
  }

  def write(s: String) {
    file.println(s)
  }
}

object PFVisualizer {
  val LOG = org.apache.log4j.Logger.getLogger("cs470.visualizer.pf")
}