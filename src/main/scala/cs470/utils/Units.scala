package cs470.utils

import java.lang.Math._

trait Units {
	def rad2deg(rad: Double) = rad * 180 / PI

	def deg2rad(deg: Double) = deg * PI / 180
}