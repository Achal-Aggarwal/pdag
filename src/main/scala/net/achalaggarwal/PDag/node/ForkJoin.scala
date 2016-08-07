package net.achalaggarwal.pdag.node

import net.achalaggarwal.pdag.Node

case class ForkJoin(nodes: Set[Node]) extends Node {
  override def canExecuteInSerialFirst(newActionNode: ActionNode): Boolean = {
    nodes.forall(_.canExecuteInSerialFirst(newActionNode))
  }

  override def canExecuteInSerialLast(newActionNode: ActionNode): Boolean = {
    nodes.forall(_.canExecuteInSerialLast(newActionNode))
  }

  override def canExecuteInParallelCompletely(newActionNode: ActionNode): Boolean = {
    val nonParallelBranches: Set[Node] = nodes
      .filter(!_.canExecuteInParallelCompletely(newActionNode))

    nonParallelBranches.isEmpty
  }

  override def canExecuteInParallelPartially(newActionNode: ActionNode): Boolean = {
    val nonParallelBranches: Set[Node] = nodes
      .filter(!_.canExecuteInParallelCompletely(newActionNode))

    (nonParallelBranches.size != nodes.size &&
      (nonParallelBranches.forall(_.canExecuteInSerialFirst(newActionNode)) ||
        nonParallelBranches.forall(_.canExecuteInSerialLast(newActionNode)))) ||
    nonParallelBranches.size == 1 && (
      nonParallelBranches.head.canExecuteInParallelPartially(newActionNode) ||
      nonParallelBranches.head.canExecuteInSerial(newActionNode))
  }

  override def consume(node: ActionNode): Node = {

    val completelyParallelBranches: Set[Node] = nodes
      .filter(_.canExecuteInParallelCompletely(node))

    val partiallyParallelBranches: Set[Node] = completelyParallelBranches
      .filter(_.canExecuteInParallelPartially(node))

    val completelyNonParallelBranches: Set[Node] = nodes
      .filter(!_.canExecuteInParallelCompletely(node))


    if (completelyParallelBranches.size == nodes.size) {
      ForkJoin.get(nodes + node)
    } else if(completelyNonParallelBranches.size == 1){
      ForkJoin.get(completelyParallelBranches + completelyNonParallelBranches.head.consume(node))
    } else if (completelyParallelBranches.nonEmpty && completelyNonParallelBranches.forall(_.canExecuteInSerialFirst(node))) {
      ForkJoin.get(completelyParallelBranches + new Branch(node, new ForkJoin(completelyNonParallelBranches)))
    } else if (completelyParallelBranches.nonEmpty && completelyNonParallelBranches.forall(_.canExecuteInSerialLast(node))) {
      ForkJoin.get(completelyParallelBranches + new Branch(new ForkJoin(completelyNonParallelBranches), node))
    } else if(canExecuteInSerialFirst(node)) {
      new Branch(node, this)
    } else {
      new Branch(this, node)
    }
  }

  override def weight: Int = nodes.toList.sortBy(_.weight).last.weight

  override def representation: String = {
    s"(${nodes.map(_.representation).mkString("|")})"
  }
}

object ForkJoin {
  def get(nodes: Set[Node]):Node = {
    if (nodes.size == 1) {
      nodes.head
    } else {
      new ForkJoin(nodes)
    }
  }
}