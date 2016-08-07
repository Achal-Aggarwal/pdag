package net.achalaggarwal.pdag

case class Action(name: Int, weight: Int, dependencies: Set[Action]) {
  val allDependencies : Set[Action] =
    dependencies.foldLeft(Set.empty[Action])((allDep, dep) => allDep.+(dep).++(dep.allDependencies))

  def this(name: Int, weight: Int, dependencies: Action*){
    this(name, weight, dependencies.toSet)
  }

  def representation = name.toString
}
