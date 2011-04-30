package cs470.utils

import java.lang.Math._

object Angle {
	def radian(rad : Double) = new Radian(rad)
	def degree(deg : Double) = new Degree(deg)

	implicit def angleAsDouble(angle : Angle) = angle.value
}

trait Angle {
	val value : Double

	def degree : Degree
	def radian : Radian

	def +(angle : Angle) : Angle
	def -(angle : Angle) : Angle
}

class Degree(private val deg : Double) extends Angle {
	val value = deg

	def radian = new Radian(deg * PI / 180)
	def degree = this

	def +(angle : Angle) = new Degree(angle.degree.deg + deg)
	def -(angle : Angle) = new Degree(angle.degree.deg - deg)

	override def toString = deg + " degrees"
}

class Radian(private val rad : Double) extends Angle {
	val value = rad

	def degree = new Degree(rad * 180 / PI)
	def radian = this

	def +(angle: Angle) = new Radian(angle.radian.rad + rad)
	def -(angle: Angle) = new Radian(angle.radian.rad - rad)

	override def toString = rad + " radians"
}
