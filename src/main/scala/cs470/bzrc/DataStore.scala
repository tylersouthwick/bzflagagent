package cs470.bzrc

import cs470.domain.UsableOccgrid
import cs470.utils.Properties
import cs470.domain.Constants._

object DataStore {
	val LOG = org.apache.log4j.Logger.getLogger(classOf[DataStore])
}

class DataStore(val queue : BzrcQueue) {
	val enemies = new RefreshableEnemies(queue)
	val tanks = new RefreshableTanks(queue)
	val obstacles = queue.invokeAndWait(_.obstacles)
	val flags = new RefreshableFlags(queue)
	val constants = queue.invokeAndWait(_.constants)
	val bases = queue.invokeAndWait(_.bases)
	val occgrid = new UsableOccgrid(Properties("occgrid.discretize", 200), obstacles, constants("tankradius"), constants("worldsize"), Seq[Enemy]())

	RefreshableData.waitForNewData()


	import DataStore._
	if (LOG.isDebugEnabled) {
		flags.foreach {flag =>
			LOG.debug("flag: " + flag)
		}
	}
			new cs470.visualization.Visualizer {
				val samples = 25
				val plotTitle = "obstacles"
				val fileName = "obstacles"
				val name = "obstacles"
				val worldsize = Integer.parseInt(constants("worldsize"))
				val obstacleList = obstacles
				draw()
				plotLines()
				close()
			}
}