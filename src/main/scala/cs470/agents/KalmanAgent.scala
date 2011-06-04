package cs470.agents

import cs470.utils._

class KalmanAgent(host: String, port: Int) extends Agent(host, port) with Threading {
	def run() {
	}

}

object KalmanAgent extends AgentCreator {
	val LOG = org.apache.log4j.Logger.getLogger(classOf[ScoutAgent])

	def name = "kalman"

	def create(host: String, port: Int) = new KalmanAgent(host, port)
}
