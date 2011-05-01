package cs470.agents

import cs470.bzrc.{BzrcQueue, RefreshableTanks}

abstract class Agent(host: String, port: Int) {
  val queue = new BzrcQueue(host, port)

  val constants = queue.invokeAndWait(_.constants)
  val flags = queue.invokeAndWait(_.flags)
  val myTanks = new RefreshableTanks(queue)
  val obstacles = queue.invokeAndWait(_.obstacles)

  def run()

}

trait AgentCreator {
  def name: String

  def create(host: String, port: Int): Agent

  override def toString = name
}

object Agents {
  val all = Seq(DummyAgent,PotentialFieldAgent)
  val LOG = org.apache.log4j.Logger.getLogger("cs470.agents.Agents")

  def start(agent: String, host: String, port: Int) {
    LOG.info("Creating agent: " + agent)
    val agents = all.filter(_.name == agent).map(_.create(host, port))
    LOG.info("Created agents " + agents)
    agents.foreach(_.run)
  }
}
