package cs470.visualization

import cs470.movement.PotentialFieldGenerator
import java.io.{FileOutputStream, PrintWriter}
import cs470.domain.{Polygon, Point}

class PFVisualizer(pfgenerator: PotentialFieldGenerator, filename: String, obstacles: Seq[Polygon], worldsize: Int, samples: Int) {

  import PFVisualizer._

  private val vec_len = 0.75 * worldsize / samples

  LOG.debug("Opening file for visualization for potential fields to: " + filename)
  private val file = new PrintWriter(new FileOutputStream(filename))

  setGnuPlotHeader()
  drawObjects()
  plotField()

  LOG.debug("Saving PF visualization to file: " + filename)
  file.close()
  LOG.info("Visualization for potential fields saved to: " + filename)

  private def drawObjects() {
    write("unset arrow")
    obstacles.foreach {
      obstacle =>
        obstacle.edges.foreach {
          case (p1, p2) =>
            write("set arrow from " + p1.x + ", " + p1.y + " to " + p2.x + ", " + p2.y + " nohead lt 3")
        }
    }
  }

  private def plotField() {
    write("plot '-' with vectors head")

    val diff: Int = worldsize / samples
    val samples2: Int = samples / 2 + 1
    val offset = new Point(worldsize / 2, worldsize / 2)

    val grid: Seq[Point] = (1 to samples).foldLeft(Seq[Point]()) {
      (points, x) =>
        points ++ (1 to samples).map(
          y => (new Point(x * diff, y * diff)) - offset
        )
    }

    grid.foreach {
      point =>
        val vector = pfgenerator.getPFVector(point)
        val mag = vector.magnitude
        if (mag != 0) {
          val endpoint = (if (mag > 1) vector / mag else vector) * vec_len
          write(" " + point.x + " " + point.y + " " + endpoint.x + " " + endpoint.y)
        }
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