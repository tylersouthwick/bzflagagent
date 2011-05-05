package cs470.bzrc

class DataStore(queue : BzrcQueue) {
	val enemies = new RefreshableEnemies(queue)
	val tanks = new RefreshableTanks(queue)
	val obstacles = queue.invokeAndWait(_.obstacles)
	val flags = new RefreshableFlags(queue)
	val constants = queue.invokeAndWait(_.constants)
	val bases = queue.invokeAndWait(_.bases)

	RefreshableData.waitForNewData()

	flags.foreach {flag =>
		println("flag: " + flag)
	}
}