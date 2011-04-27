package cs470

object Engine {
	def start(host : String, port : Int) = new Engine(host, port).start
}

class Engine(host : String, port : Int) {
	val con = new BzFlagConnection(host, port)

	def start {
		val tanks = con.mytanks

/*
		tanks.foreach{tank =>
			con.speed(tank.id, 1)
		}
		con.constants
		con.othertanks
		con.mytanks
		con.teams
		con.obstacles
		con.shots
		con.flags
		con.bases
		*/

        con.obstacles
        con.bases
        con.flags
        con.occgrid(0)
	}
}

// vim: set ts=4 sw=4 et:
