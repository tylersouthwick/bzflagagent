package cs470

import org.apache.commons.cli._
import org.apache.log4j.Logger

object Main {
	val LOG = Logger.getLogger("cs470.Main")

	def main(args : Array[String]) {
		setupLog4j

		val parser = new PosixParser
		val cmd = parser.parse(options, args)

		val port = {
			if (!cmd.hasOption("p")) {
				LOG.error("Must specify a port")
				System.exit(-1)
			}
			try {
				Integer.parseInt(cmd.getOptionValue("p"))
			} catch {
				case _ : Throwable => {
					LOG.error("Invalid port")
					System.exit(-1)
					0
				}
			}
		}
		val host = {
			if (!cmd.hasOption("h")) {
				LOG.error("Must specify a host")
				System.exit(-1)
			}
			cmd.getOptionValue("h")
		}

		Engine.start(host, port)
	}

	val options = new Options
	options.addOption("p", true, "port")
	options.addOption("h", true, "host")

	def setupLog4j {
		org.apache.log4j.BasicConfigurator.configure
	}
}

// vim: set ts=4 sw=4 et:
