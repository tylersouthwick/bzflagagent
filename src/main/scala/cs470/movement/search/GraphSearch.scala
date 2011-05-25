package cs470.movement.search

import collection.mutable.HashSet

trait GraphSearch {

  val explored = new HashSet[(Int, Int)]
  val start: (Int, Int)

  def find() {

  }
}