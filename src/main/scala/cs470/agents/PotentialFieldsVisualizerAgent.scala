package cs470.agents

import cs470.movement.{PotentialFieldGenerator, PotentialFieldConstants}
import PotentialFieldConstants._
import cs470.domain._
import Constants._
import cs470.visualization.PFVisualizer

class PotentialFieldsVisualizerAgent(host: String, port: Int) extends Agent(host, port) {
  val worldSize: Int = constants("worldsize")

  def run() {
    val baseGoalColor = "green"
    val flagAndObstaclesField = new PotentialFieldGenerator(store = store) {
      val base = bases.find(_.color == baseGoalColor).get

      def getPathVector(point: Point) = {
        val toBase = AttractivePF(point, base.points.center, r.base, s.base, alpha.base)

        new Vector(toBase + getFieldForObstacles(point) + randomVector)
      }
    }

    val flagField = new PotentialFieldGenerator(store = store) {
      val base = bases.find(_.color == baseGoalColor).get

      def getPathVector(point: Point) = new Vector(AttractivePF(point, base.points.center, r.base, s.base, alpha.base))
    }

    val obstaclesField = new PotentialFieldGenerator(store = store) {
      def getPathVector(point: Point) = new Vector(getFieldForObstacles(point))
    }
    val obstaclesTangentialField = new PotentialFieldGenerator(store = store) {
      def getPathVector(point: Point) = new Vector(getFieldForObstaclesTangential(point, true))
    }

    new PFVisualizer {
      val samples = 25
      val pathFinder = obstaclesField
      val plotTitle = ""
      val fileName = "pfObstacles"
      val name = "pfObstacles"
      val worldsize = worldSize
      val obstacleList = obstacles
    }.draw()

    new PFVisualizer {
      val samples = 25
      val pathFinder = obstaclesTangentialField
      val plotTitle = ""
      val fileName = "pfObstaclesTangential"
      val name = "pfObstaclesTangential"
      val worldsize = worldSize
      val obstacleList = obstacles
    }.draw()

    new PFVisualizer {
      val samples = 25
      val pathFinder = flagField
      val plotTitle = ""
      val fileName = "pfFlag"
      val name = "pfFlag"
      val worldsize = worldSize
      val obstacleList = obstacles
    }.draw()

    new PFVisualizer {
      val samples = 25
      val pathFinder = flagAndObstaclesField
      val plotTitle = ""
      val fileName = "pfFlagsAndObstacles"
      val name = "pfFlagsAndObstacles"
      val worldsize = worldSize
      val obstacleList = obstacles
    }.draw()
    //		new PFVisualizer(obstaclesField, "pfObstacles.gpi", obstacles, worldsize, 25)
    //		new PFVisualizer(obstaclesTangentialField, "pfObstaclesTangential.gpi", obstacles, worldsize, 25)
    //		new PFVisualizer(flagField, "pfFlag.gpi", obstacles, worldsize, 25)
    //		new PFVisualizer(flagAndObstaclesField, "pfFlagsAndObstacles.gpi", obstacles, worldsize, 25)
  }
}

object PotentialFieldsVisualizerAgent extends AgentCreator {
  def create(host: String, port: Int) = new PotentialFieldsVisualizerAgent(host, port)

  def name = "PF Visualizer"
}
