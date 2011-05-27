package cs470.visualization

import cs470.domain.{Polygon, Point}
import cs470.movement.{FindAgentPath}
import java.io.{BufferedOutputStream, FileOutputStream, PrintWriter}
import cs470.utils.Properties
import java.util.concurrent.atomic.AtomicInteger

object Color extends Enumeration {
  type Color = Value

  val BLACK = Value("black")
  val RED = Value("red")
  val GREEN = Value("green")
  val BLUE = Value("blue")
  val PURPLE = Value("purple")
  val AQUA = Value("aqua")
  val BROWN = Value("brown")
  val ORANGE = Value("orange")
  val LIGHT_BROWN = Value("light_brown")
}

trait PFVisualizer extends Visualizer {
  val pathFinder: FindAgentPath

  val samples: Int

  private lazy val vec_len = 0.75 * worldsize.asInstanceOf[Double] / samples.asInstanceOf[Double]


  override def draw() {
    super.draw()
    close()
  }

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

trait SearchVisualizer extends Visualizer {

  private val showEvery = Properties("vis.showEvery", 25)
  private val pauseView = Properties("vis.pauseView", 1000)
  private val delay = 0.1
  private var pauseViewCounter = 0
  private var showEveryCounter = 0

  def drawSearchNodes(nodes: Traversable[(Point, Point)]) {
    nodes.foreach {
      case (p1, p2) =>
        if (showEvery == showEveryCounter) {
          drawLine(p1, p2, Color.ORANGE)
          showEveryCounter = 0
        } else {
          showEveryCounter = showEveryCounter + 1
        }
    }

    flush()

    if (pauseViewCounter == pauseView) {
      pause()
      pauseViewCounter = 0
    } else {
      pauseViewCounter = pauseViewCounter + 1
    }
  }

  def drawPoint(point: Point, color: Color.Color) {
    write("plot " + point.x + "," + point.y + " with points")
  }

  def pause() {
    saveType match {
      case "" => {
        plotLines()
        write("pause " + delay)
      }
      case _ => flush();
    }
  }

  def clear() {
    write("clear")
    pause()
  }

  def drawFinalPath(nodes: Seq[(Point, Point)]) {
    nodes.foreach {
      case (p1, p2) =>
        drawLine(p1, p2, Color.BLACK)
    }
    plotLines()
    close()
  }

  override def plot() {

  }

}

object Visualizer {
	private val ai = new AtomicInteger
	def count = ai.incrementAndGet()
}

trait Visualizer {
  val LOG = org.apache.log4j.Logger.getLogger("cs470.visualization.visualizer")

  val obstacleList: Seq[Polygon]

  val worldsize: Int

  val name: String

  val fileName: String

  val plotTitle: String

  val saveType = Properties("visualizer.saveType", "")

  private lazy val filename = fileName + "_" + Visualizer.count + ".gpi"

  private lazy val file = new PrintWriter(new BufferedOutputStream(new FileOutputStream(filename)))

  def draw() {
    LOG.info("Opening file for visualization for " + name + " to: " + filename)

    saveInfo()
    setGnuPlotHeader()
    drawObjects()
    plot()
  }

  def plotLines() {
    write("plot '-' with lines")
    write("0 0 0 0")
    write("e")
    flush();
  }

  def saveInfo() {
    saveType match {
      case "eps" => {
        write("set term post eps")
        write("set output \"" + fileName + ".eps\"")
      }
      case "png" => {
        write("set term png")
        write("set output \"" + fileName + ".png\"")
      }
      case "" => ""
      case ext => LOG.warn("Save type " + ext + " is unknown.")
    }
  }

  def drawLine(p1: Point, p2: Point, color: Color.Color) {
    write("set arrow from " + p1.x + ", " + p1.y + " to " + p2.x + ", " + p2.y + " nohead lt 1 lc rgb \"" + color + "\"")
  }

  private def drawObjects() {
    write("unset arrow")
    obstacleList.foreach {
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
    write("set title '" + plotTitle + "'")
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
