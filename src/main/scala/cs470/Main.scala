package cs470

import org.apache.commons.cli._
import org.apache.log4j.Logger
import agents.Agents

object Main {
	val LOG = Logger.getLogger("cs470.Main")
    val DEFAULT_HOST = "localhost"
    val DEFAULT_PORT = "9000";
    val DEFAULT_AGENT = "dummy"

    implicit def findAttribute(cmd : CommandLine) = new {
        def findAttribute (name : String, error : => String, default : String) = {
            if (cmd.hasOption(name)) {
                cmd.getOptionValue(name)
            } else {
                val property = "ai." + name
                LOG.debug("reading system property: " + property)
                val v = System.getProperty(property)
                if (v == null) {
                    LOG.warn("using default " + name + ": " + default)
                    default
                } else v
            }
        }
    }

	def main(args : Array[String]) {
		val parser = new PosixParser
		val cmd = parser.parse(options, args)

		setupLog4j()

		val port = Integer.parseInt(cmd.findAttribute("p", "Must specify port", DEFAULT_PORT))
		val host = cmd.findAttribute("h", "Must specify host", DEFAULT_HOST)
        val agent = cmd.findAttribute("a", "Must specify an agent", DEFAULT_AGENT)

		Agents.start(agent, host, port)
	}

	val options = new Options
	options.addOption("p", true, "port")
	options.addOption("h", true, "host")
	options.addOption("a", true, "Which Agent " + Agents.all)

	def setupLog4j() {
        org.apache.log4j.PropertyConfigurator.configure(classOf[Options].getResource("/logging/log4j.properties"))
	}

}

// vim: set ts=4 sw=4 et:
