package cs470.visualizer

import scala.swing._
import scala.Array._
import cs470.domain.UpdateableOccgrid
import com.sun.org.apache.xerces.internal.parsers.CachingParserPool.SynchronizedGrammarPool
import javax.swing.{Timer, JPanel}
import java.awt.event.{ActionEvent, ActionListener}

/**
 * @author tylers3
 */

//import cs470.domain.BayesianOccgrid.
trait BayesianVisualizer extends UpdateableOccgrid {
	private lazy val visualizer = new SwingOccgridRealVisualizer(this, size, lock);

	def startVisualizer() {
		visualizer.main(Array(""))

		new Timer(500, new ActionListener {
			def actionPerformed(e: ActionEvent) {
		visualizer.updateImage()
			}
		})
	}

}

class SwingOccgridRealVisualizer(data : (Int, Int) => Double, worldsize : Int, lock : Object) extends SimpleSwingApplication {
  val LOG = org.apache.log4j.Logger.getLogger("cs470.visualizer.swing")

	def top = new MainFrame {
		title = "Searching Visualizer"
		/*
		val x = newField
		val y = newField
		val percentage = newField
		*/
		val update = new Button {
			action = Action("Update") {
				//updateData(x.value, y.value, percentage.value / 100.0)
			}
		}
		def label(s : String) = new Label(s  +" = ");
		val updatePanel = new FlowPanel(/*label("x"), x.field, label("y"), y.field, label("percentage"), percentage.field, */update)
		contents = new BorderPanel {
			add(updatePanel, BorderPanel.Position.North)
			add(new ScrollPane(Component.wrap(image)), BorderPanel.Position.Center)
		}
	}

	def time = new java.util.Date().getTime

	val image = {
		import java.awt._
		import java.awt.image._
		import scala.collection.JavaConversions._
		val panel = new JPanel {
			val grayscale : Seq[Color] = (0 to 100).foldLeft(new java.util.ArrayList[Color]()) { (seq, num) => {
				val color = 255 - (num * 2.55).asInstanceOf[Int]
				seq.add(new Color(color, color, color))
				seq
			}}

			override def paint(g: Graphics) {
				super.paint(g)

				val start = time

				val bufferedImage = new BufferedImage(worldsize, worldsize, BufferedImage.TYPE_INT_ARGB)
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
				g.drawImage(bufferedImage, 0, 0, this)
				val end = time

				LOG.debug("Redrew in " + (end - start) + "ms")
			}
		}
		panel.setPreferredSize(new java.awt.Dimension(worldsize, worldsize))
		panel
	}

	def updateImage() {
		image.repaint()
	}
}