package net.achalaggarwal.pdag.node

import net.achalaggarwal.pdag.{Node, Action}

case class ActionNode(action: Action) extends Node {
  override def weight: Int = action.weight

  override def consume(newActionNode: ActionNode): Node = {
    if (canExecuteInParallelCompletely(newActionNode)) {
      consumeParallely(newActionNode)
    } else {
      consumeSerially(newActionNode)
    }
  }

  def consumeSerially(newActionNode: ActionNode): Node = {
    if (canExecuteInSerialFirst(newActionNode)) {
      new Branch(List(newActionNode, this))
    } else if(canExecuteInSerialLast(newActionNode)) {
      new Branch(List(this, newActionNode))
    } else {
      null
    }
  }

  override def canExecuteInSerialFirst(newActionNode: ActionNode): Boolean = {
    !newActionNode.action.allDependencies.contains(action)
  }

  override def canExecuteInSerialLast(newActionNode: ActionNode): Boolean = {
    !action.allDependencies.contains(newActionNode.action)
  }

  def consumeParallely(newActionNode: ActionNode): Node = {
    new ForkJoin(Set(this, newActionNode))
  }

  override def canExecuteInParallelCompletely(newActionNode: ActionNode): Boolean = {
    !(action.allDependencies.contains(newActionNode.action) ||
      newActionNode.action.allDependencies.contains(action))
  }

  override def canExecuteInParallelPartially(newActionNode: ActionNode): Boolean = {
    canExecuteInParallelCompletely(newActionNode)
  }

  override def representation: String = action.representation
}