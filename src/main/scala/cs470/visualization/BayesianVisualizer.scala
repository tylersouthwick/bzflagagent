package cs470.visualization

import scala.swing._
import cs470.domain._
import javax.swing.{Timer, JPanel}
import java.awt.event.{ActionEvent, ActionListener}
import javax.imageio.ImageIO
import java.io.IOException

/**
 * @author tylers3
 */

trait BayesianVisualizer extends UpdateableOccgrid with PolygonFinder {
	private lazy val visualizer = new SwingOccgridRealVisualizer(this, size, lock, this);

	def startVisualizer() {
		visualizer.main(Array(""))
		val timer = new Timer(500, new ActionListener {
			def actionPerformed(e: ActionEvent) {
				visualizer.updateImage()
			}
		})
		timer.setInitialDelay(0)
		timer.start()
	}

}

class SwingOccgridRealVisualizer(data: (Int, Int) => Double, worldsize: Int, lock: Object, occgrid : UpdateableOccgrid with PolygonFinder) extends SimpleSwingApplication {
	val LOG = org.apache.log4j.Logger.getLogger("cs470.visualizer.swing")

	def top = new MainFrame {
		title = "Searching Visualizer"
		val save = new Button {
			action = Action("Save") {
				val chooser = new FileChooser(new java.io.File(".")) {
					title = "File Chooser"
					multiSelectionEnabled = false
				}
				chooser.showSaveDialog(this) match {
					case FileChooser.Result.Approve => {
						try {
							ImageIO.write(bufferedImage, "png", chooser.selectedFile)
						} catch {
							case ioe : IOException => LOG.error("There was an error writing file")
						}
					}
					case _ =>
				}
			}
		}
		val polygons = new Button {
			action = Action("Show Polygons") {
				val corners = occgrid.polygons
				if (corners.isEmpty) println("No corners found")
				else {
					println("Corners:")
					corners.foreach(t => println("\t" + t)) 
					val filename = "polygon_view"
					val myworldsize = worldsize
					val outputName = new cs470.visualization.Visualizer {
						val samples = 25
						val plotTitle = filename
						val fileName = filename
						val name = filename
						val worldsize = myworldsize
						val obstacleList = corners
						draw()
						plotLines()
						close()
					}.outputName
					Runtime.getRuntime.exec(Array("gnuplot", "-persist", outputName)).waitFor()
				}
			}
		}

		def label(s: String) = new Label(s + " = ");
		val updatePanel = new FlowPanel(save, polygons)
		contents = new BorderPanel {
			add(updatePanel, BorderPanel.Position.North)
			add(new ScrollPane(Component.wrap(image)), BorderPanel.Position.Center)
		}
	}

	private def time = new java.util.Date().getTime

	import java.awt._
	import java.awt.image._
	import scala.collection.JavaConversions._

	private val grayscale: Seq[Color] = (0 to 100).foldLeft(new java.util.ArrayList[Color]()) {
		(seq, num) => {
			val color = 255 - (num * 2.55).asInstanceOf[Int]
			seq.add(new Color(color, color, color))
			seq
		}
	}

	def bufferedImage = {
		val bufferedImage = new BufferedImage(worldsize - 1, worldsize - 1, BufferedImage.TYPE_INT_ARGB)
		val graphics = bufferedImage.getGraphics.asInstanceOf[Graphics2D]
		graphics.setColor(java.awt.Color.BLACK)
		for (x <- 0 until worldsize - 1) {
			for (y <- 0 until worldsize - 1) {

				//graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (data(x)(y)).asInstanceOf[Float]))
				def percent = {
					var percent = 0
					lock synchronized {
						percent = java.lang.Math.floor(data(x, y) * 100).asInstanceOf[Int]
					}
					percent
				}
				//println("percent [" + percent + "] -> " + grayscale(percent))
				graphics.setColor(grayscale(percent))
				graphics.drawLine(x, y, x, y)
			}
		}
		bufferedImage
	}

	val image = {
		val panel = new JPanel {
			val grayscale: Seq[Color] = (0 to 100).foldLeft(new java.util.ArrayList[Color]()) {
				(seq, num) => {
					val color = 255 - (num * 2.55).asInstanceOf[Int]
					seq.add(new Color(color, color, color))
					seq
				}
			}

			override def paint(g: Graphics) {
				LOG.debug("Redrawing screen")
				super.paint(g)

				val start = time

				g.drawImage(bufferedImage, 0, 0, this)
				val end = time

				LOG.debug("Redrew in " + (end - start) + "ms")
			}
		}
		panel.setPreferredSize(new java.awt.Dimension(worldsize - 1, worldsize - 1))
		panel
	}

	def updateImage() {
		image.repaint()
	}
}
