package cs470

import agents._
import bzrc.{DataStore, BzrcQueue}
import org.apache.commons.cli._
import org.apache.log4j.Logger
import utils.Properties

object Main {
	val LOG = Logger.getLogger("cs470.Main")
	val DEFAULT_HOST = "localhost"
	val DEFAULT_PORT = "9000";

	implicit def findAttribute(cmd: CommandLine) = new {
		def findAttribute(name: String, error: => String, default: String): String = {
			if (cmd.hasOption(name)) {
				cmd.getOptionValue(name)
			} else {
				Properties("ai." + name, default)
			}
		}
	}

	def main(args: Array[String]) {
		if (args.length == 1 && args.apply(0) == "help") {
			val formatter = new HelpFormatter
			formatter.printHelp("agents", options)
		} else {
			try {
				doMain(args)
			} catch {
				case t : Throwable => {
					LOG.error("Unable to start", t)
				}
			}
		}
	}

	def doMain(args: Array[String]) {
		val parser = new PosixParser
		val cmd = parser.parse(options, args, true)

		setupLog4j()

		val port = Integer.parseInt(cmd.findAttribute("p", "Must specify port", DEFAULT_PORT))
		val host = cmd.findAttribute("h", "Must specify host", DEFAULT_HOST)

		val queue = new BzrcQueue(host, port)

		Agent(new DataStore(queue))
	}

	val options = new Options
	options.addOption("p", true, "port")
	options.addOption("h", true, "host")

	def setupLog4j() {
		org.apache.log4j.PropertyConfigurator.configure(classOf[Options].getResource("/logging/log4j.properties"))
	}

}

// vim: set ts=4 sw=4 et:
