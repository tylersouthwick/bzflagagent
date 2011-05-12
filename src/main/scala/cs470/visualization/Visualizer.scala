package cs470.visualization

import java.io.{FileOutputStream, PrintWriter}
import cs470.domain.{Polygon, Point}
import cs470.movement.{FindAgentPath}

object Color extends Enumeration {
  type Color = Value

  val BLACK = Value("-1")
  val RED = Value("1")
  val GREEN = Value("2")
  val BLUE = Value("3")
  val PURPLE = Value("4")
  val AQUA = Value("5")
  val BROWN = Value("6")
  val ORANGE = Value("7")
  val LIGHT_BROWN = Value("8")
}

class PFVisualizer(pathFinder: FindAgentPath, filename: String, obstacles: Seq[Polygon], worldsize: Int, samples: Int) extends Visualizer(filename, obstacles, worldsize, "PF") {

  private lazy val vec_len = 0.75 * worldsize.asInstanceOf[Double] / samples.asInstanceOf[Double]

  close()

  override def plot() {
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
        val vector = pathFinder.getPathVector(point)
        val mag = vector.magnitude
        if (mag != 0) {
          val endpoint = (if (mag > 1) vector / mag else vector) * vec_len
          write(" " + point.x + " " + point.y + " " + endpoint.x + " " + endpoint.y)
        }
    }

    write("e")

  }
}

class SearchVisualizer(filename: String, obstacles: Seq[Polygon], worldsize: Int) extends Visualizer(filename, obstacles, worldsize, "search") {
  private val delay = 0.000001

  def drawSearchNodes(nodes : Traversable[(Point,Point)]) {
	  nodes.foreach{case (p1, p2)=>
		  drawLine(p1,p2,Color.ORANGE)
	  }
	  pause()
  }

	def plotLines() {
		write("plot '-' with lines")
		write("0 0 0 0")
		write("e")
	}

  def pause() {
	  plotLines()
	  //write("pause " + delay)
	  //flush();
  }

  def drawFinalPath(nodes: Seq[(Point, Point)]) {
    nodes.foreach {
      case (p1, p2) =>
        drawLine(p1, p2, Color.ORANGE)
    }
	  plotLines()
    close()
  }

  override def plot() {

  }

}

abstract class Visualizer(filename: String, obstacles: Seq[Polygon], worldsize: Int, name: String) {

  import cs470.visualization.Visualizer._

  println("Opening file for visualization for " + name + " to: " + filename)
  private val file = new PrintWriter(new FileOutputStream(filename))

  setGnuPlotHeader()
  drawObjects()
  plot()

  LOG.debug("Saving " + name + " visualization to file: " + filename)

  def drawLine(p1: Point, p2: Point, color: Color.Color) {
    write("set arrow from " + p1.x + ", " + p1.y + " to " + p2.x + ", " + p2.y + " nohead lt " + color)
  }

  private def drawObjects() {
    write("unset arrow")
    obstacles.foreach {
      obstacle =>
        obstacle.edges.foreach {
          case (p1, p2) =>
            drawLine(p1, p2, Color.BLUE)
        }
    }
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

  def plot() {}

	def flush() {
		file.flush()
	}
  def close() {
    file.close()
    LOG.info("Visualization for " + name + " saved to: " + filename)
  }
}

object Visualizer {
  val LOG = org.apache.log4j.Logger.getLogger("cs470.visualizer")
}