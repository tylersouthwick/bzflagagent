package cs470.agents

import cs470.bzrc.{DataStore, BzrcQueue}

abstract class Agent(host: String, port: Int) {
  val queue = new BzrcQueue(host, port)
  val store = new DataStore(queue)

  val constants = store.constants
  val flags = store.flags
  val myTanks = store.tanks
  val obstacles = store.obstacles
  val enemies = store.enemies
  val bases = store.bases

  def run()

}

trait AgentCreator {
  def name: String

  def create(host: String, port: Int): Agent

  override def toString = name
}

object Agents {
  val all = Seq(DummyAgent, PotentialFieldAgent, PotentialFieldsVisualizerAgent, SearchLabAgent, MultiAgent, ScoutAgent, KalmanAgent, KalmanPigeonsAgent)
  val LOG = org.apache.log4j.Logger.getLogger("cs470.agents.Agents")

  def start(agent: String, host: String, port: Int) {
    LOG.info("Creating agent: " + agent)
    val agents = all.filter(_.name == agent).map(_.create(host, port))
    agents.foreach(_.run())
    if (agents.isEmpty) {
      LOG.error("Could not find an agent with name: " + agent)
    }
  }
}
