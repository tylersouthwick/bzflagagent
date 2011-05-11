package cs470.agents

class SearchLabAgent(host : String, port : Int) extends Agent(host, port) {
	def run() {
		queue.invokeAndWait(_.occgrid(0))
	}
}

object SearchLabAgent extends AgentCreator {
	def create(host: String, port: Int) = new SearchLabAgent(host, port)

	def name = "search"
}

