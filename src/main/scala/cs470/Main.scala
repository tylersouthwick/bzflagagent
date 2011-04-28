package cs470

import org.apache.commons.cli._
import org.apache.log4j.Logger
import agents.Agents

object Main {
	val LOG = Logger.getLogger("cs470.Main")
  val HOST = "localhost"

	def main(args : Array[String]) {
		setupLog4j

		val parser = new PosixParser
		val cmd = parser.parse(options, args)

		val port = {
			if (!cmd.hasOption("p")) {
				LOG.error("Must specify a port")
				System.exit(-1)
				0
			} else {
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
		}

		val host = {
			if (!cmd.hasOption("h")) {
				LOG.error("Must specify a host")
				LOG.info("Using default host: " + HOST)
				HOST
			} else {
			  cmd.getOptionValue("h")
			}
		}

        val agent = {
            if (!cmd.hasOption("a")) {
                LOG.error("Must specify an agent: " + Agents.all)
                System.exit(-1)
                ""
            } else {
                cmd.getOptionValue("a")
            }
        }

		Agents.start(agent, host, port)
	}

	val options = new Options
	options.addOption("p", true, "port")
	options.addOption("h", true, "host")
	options.addOption("a", true, "Which Agent " + Agents.all)

	def setupLog4j {
		org.apache.log4j.BasicConfigurator.configure
	}
}

// vim: set ts=4 sw=4 et:
