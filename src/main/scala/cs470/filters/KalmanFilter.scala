package cs470.filters

import org.ejml.simple.SimpleMatrix
import cs470.utils.Properties
import org.ejml.alg.dense.decomposition.DecompositionFactory
import org.ejml.ops.CommonOps
import cs470.bzrc.Enemy
import java.util.Date
import cs470.domain.Point

case class KalmanFilter(enemy : Enemy) {
	var mu = new SimpleMatrix(Array(Array(0.0, 0.0, 0.0, 0.0, 0.0, 0.0))).transpose()

	var sigma : SimpleMatrix = {
		val data = Array.ofDim[Double](6, 6)
		def setValue(row : Int, value : Double) {
			data(row)(row) = value
		}
		setValue(0, 100)
		setValue(1, .1)
		setValue(2, .1)
		setValue(3, 100)
		setValue(4, .1)
		setValue(5, .1)
		new SimpleMatrix(data)
	}

	val sigmaX = {
		val data = Array.ofDim[Double](6, 6)
		def setValue(row : Int, value : Double) {
			data(row)(row) = value
		}
		setValue(0, .1)
		setValue(1, .1)
		setValue(2, 100)
		setValue(3, .1)
		setValue(4, .1)
		setValue(5, 100)
		new SimpleMatrix(data)
	}

	def F(deltaT : Double) = {
		val data = Array.ofDim[Double](6, 6)
		data(0)(0) = 1
		data(0)(1) = deltaT
		data(0)(2) = java.lang.Math.pow(deltaT, 2) / 2

		data(1)(1) = 1
		data(1)(2) = deltaT

		data(2)(1) = -c
		data(2)(2) = 1

		data(3)(3) = 1
		data(3)(4) = deltaT
		data(3)(5) = java.lang.Math.pow(deltaT, 2) / 2

		data(4)(4) = 1
		data(4)(5) = deltaT

		data(5)(4) = -c
		data(5)(5) = 1
		new SimpleMatrix(data)
	}

	val H = {
		val data = Array.ofDim[Double](2, 6)
		data(0)(0) = 1
		data(1)(3) = 1
		new SimpleMatrix(data)
	}

	val positionNoise = new {
		val x = Properties("positionNoise.x", 5)
		val y = Properties("positionNoise.x", 5)
	}

	val sigmaZ = {
		val data = Array.ofDim[Double](2, 2)
		data(0)(0) = positionNoise.x * positionNoise.x
		data(1)(1) = positionNoise.y * positionNoise.y
		new SimpleMatrix(data)
	}
	val I = SimpleMatrix.wrap(CommonOps.identity(6))

	val c = Properties("frictionCoefficient", 0.1)

	implicit def simpleMatrixOverload(m : SimpleMatrix) = new {
		def *(t : SimpleMatrix) = m.mult(t)
		def +(t : SimpleMatrix) = m.plus(t)
		def -(t : SimpleMatrix) = m.minus(t)
		//def invert = SimpleMatrix.wrap(CommonOps.invert(m.getMatrix))
	}

	var xt = mu
	def time = new java.util.Date().getTime
	var lastTime = time

	def deltaT = {
		val current = time
		val d = current - lastTime
		lastTime = current
		d / 1000.0
	}

	def update() {
		val dt = deltaT
		val f = F(dt)
		val ft = f.transpose()
		val Ht = H.transpose()

		xt = sampleFromNormalDistribution(f * xt, sigmaX)
		val zt = {
			val location = enemy.location
			val data = Array(location.x, location.y)
			new SimpleMatrix(Array(data)).transpose()
		}

		val K = (f * sigma * ft + sigmaX) * Ht * (H * (f * sigma * ft + sigmaX) * Ht + sigmaZ).invert
		mu = f * mu + K * (zt - H * f * mu)
		sigma = (I - K * H) * (f * sigma * ft + sigmaX)

		visualize(dt)
	}

	private def sampleFromNormalDistribution(mu : SimpleMatrix, sigma : SimpleMatrix) = {
		val length = sigma.getMatrix.getNumRows
		val sqrtSigma = {
			val d = DecompositionFactory.chol(length, true)
			d.decompose(sigma.getMatrix)
			SimpleMatrix.wrap(d.getT(null))
		}
		val temp = new SimpleMatrix(Array(Array.fill(length){scala.util.Random.nextGaussian()}))
		mu + sqrtSigma * temp.transpose()
	}

	import java.io._
	import java.lang.Math._
	private val file = new PrintWriter(new BufferedOutputStream(new FileOutputStream("kalman.gpi")))
	file.println("set xrange [-400.0: 400.0]\nset yrange [-400.0: 400.0]\nset pm3d\nset view map\nunset key\nset size square")
	file.println("set palette model RGB functions 1-gray, 1-gray, 1-gray\n\n# How fine the plotting should be, at some processing cost:\nset isosamples 100")

	private def visualize(deltaT : Double) {
		val sigma_x = sqrt(sigma.get(0, 0))
		val sigma_y = sqrt(sigma.get(3, 3))
		val rho = sigma.get(0, 1) / (sigma_x * sigma_y)
		file.println("sigma_x = " + sigma_x)
		file.println("sigma_y = " + sigma_y)
		file.println("rho = " + rho)
		file.println("splot 1.0/(2.0 * pi * sigma_x * sigma_y * sqrt(1 - rho**2)) \\\n\t* exp(-1.0/2.0 * (x**2 / sigma_x**2 + y**2 / sigma_y**2 \\\n\t- 2.0*rho*x*y/(sigma_x*sigma_y))) with pm3d")
		file.println("pause " + deltaT)
		file.flush()
	}

	def predict(deltaT : Double) = {
		val p = F(deltaT) * mu
		new Point(p.get(0, 0), p.get(3, 0))
	}
}