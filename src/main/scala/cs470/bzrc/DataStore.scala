package cs470.bzrc

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

	RefreshableData.waitForNewData()

	import DataStore._
	if (LOG.isDebugEnabled) {
		flags.foreach {flag =>
			LOG.debug("flag: " + flag)
		}
	}
}