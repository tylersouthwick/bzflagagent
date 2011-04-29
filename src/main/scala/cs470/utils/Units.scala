package cs470.utils

import java.lang.Math._

trait Units {
	def rad2deg(rad: Float) = {
		(rad * 180 / PI).asInstanceOf[Float]
	}

	def deg2rad(deg: Float) = {
		(deg * PI / 180).asInstanceOf[Float]
	}
}