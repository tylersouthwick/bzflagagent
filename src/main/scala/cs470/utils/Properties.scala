package cs470.utils

/**
 * finds system properties
 */
object Properties {
	val LOG = org.apache.log4j.Logger.getLogger("cs470.utils.Properties")

	def apply(s : String) = {
		val property = System.getProperty(s)
		if (property == null) {
			None
		} else {
			Some(property)
		}
	}

	def apply(s : String, default : String) = convert(s, default) (t => t)
	def apply(s : String, default : Int) = convert(s, default) (Integer.parseInt(_))
	def apply(s : String, default : Boolean) = convert(s, default) ("true" == _)
	def apply(s : String, default : Double) = convert(s, default) (java.lang.Double.parseDouble(_))

	private def convert[T](s : String, default : T)(callback : (String) => T) : T = this(s) match {
		case None => use(s, default)
		case Some(s) => callback(s)
	}

	private def use[T](s : String, value : T) = {
		LOG.warn("Using default value [" + value + "] for property [" + s + "]")
		value
	}
}