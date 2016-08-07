package net.achalaggarwal.pdag

import net.achalaggarwal.pdag.node.ActionNode

trait Node {
  def canExecuteInSerialFirst(newActionNode: ActionNode): Boolean
  def canExecuteInSerialLast(newActionNode: ActionNode): Boolean

  def canExecuteInSerial(newActionNode: ActionNode) = {
    canExecuteInSerialFirst(newActionNode) || canExecuteInSerialLast(newActionNode)
  }

  def canExecuteInParallelCompletely(newActionNode: ActionNode): Boolean
  def canExecuteInParallelPartially(newActionNode: ActionNode): Boolean

  def consume(node: ActionNode): Node
  def weight: Int
  def representation: String
}
