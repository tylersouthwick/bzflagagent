package cs470.utils

import java.lang.Math._

object Angle {
	def radian(rad : Double) = Radian(rad)
	def degree(deg : Double) = Degree(deg)

	implicit def radianAsDouble(angle : Radian) = angle.value
	implicit def angleAsRadian(angle : Angle) = angle.radian
	implicit def degreeAsDouble(angle : Degree) = angle.value
	implicit def angleAsDegree(angle : Angle) = angle.degree
}

trait Angle {
	val value : Double

	def degree : Degree
	def radian : Radian

	def +(angle : Angle) : Angle
	def -(angle : Angle) : Angle
}

object Degree extends Function1[Double, Radian] {
	def apply(deg: Double) = new Degree(deg)
}

class Degree(private val deg : Double) extends Angle {
	val value = deg

	def radian = new Radian(deg * PI / 180)
	def degree = this

	def +(angle : Angle) = new Degree(deg + angle.degree.deg)
	def -(angle : Angle) = new Degree(deg - angle.degree.deg)

	override def toString = deg % 360 + " deg"
}

object Radian extends Function1[Double, Radian] {
	def apply(rad: Double) = new Radian(rad)
}

class Radian(private val rad : Double) extends Angle {
	val value = rad

	def degree = new Degree(rad * 180 / PI)
	def radian = this

	def +(angle: Angle) = new Radian(rad + angle.radian.rad)
	def -(angle: Angle) = new Radian(rad - angle.radian.rad)

	override def toString = rad + " rad"
}
