package net.achalaggarwal.pdag.node

import net.achalaggarwal.pdag.Node

case class Branch(nodes: List[Node]) extends Node {
  def this(nodes: Node*) = this(nodes.toList)
  override def canExecuteInSerialFirst(newActionNode: ActionNode): Boolean = {
    nodes.forall(_.canExecuteInSerialFirst(newActionNode))
  }

  override def canExecuteInSerialLast(newActionNode: ActionNode): Boolean = {
    nodes.forall(_.canExecuteInSerialLast(newActionNode))
  }

  override def canExecuteInSerial(newActionNode: ActionNode): Boolean = {
    nodes.exists(_.canExecuteInSerial(newActionNode))
  }

  override def canExecuteInParallelCompletely(newActionNode: ActionNode): Boolean = {
    nodes.forall(_.canExecuteInParallelCompletely(newActionNode))
  }

  override def canExecuteInParallelPartially(newActionNode: ActionNode): Boolean = {
    nodes.exists(_.canExecuteInParallelPartially(newActionNode))
  }

  override def consume(node: ActionNode): Node = {
    if (canExecuteInParallelCompletely(node)) {
      new ForkJoin(Set(node, this))
    } else if (canExecuteInParallelPartially(node)) {
      val components = nodes.tail.foldLeft((List.empty[Node], List(nodes.head)))(extractComponents(node))._1

      val slowestParallelBranch =
        components
        .filter(_.canExecuteInParallelPartially(node))
        .sortBy(-_.weight)
        .head

      Branch.get(components.map(replaceNodeWith(slowestParallelBranch, slowestParallelBranch.consume(node))))
    } else if(canExecuteInSerialFirst(node)) {
      Branch.get(node +: nodes)
    } else if(canExecuteInSerialLast(node)) {
      Branch.get(nodes :+ node)
    } else {
      val serialExecutableNode =
        nodes
          .zip(nodes.tail)
          .find(lr => lr._1.canExecuteInSerialLast(node) && lr._2.canExecuteInSerialFirst(node))
          .get
          ._1

      Branch.get(nodes.flatMap(n => {
        if (n == serialExecutableNode) {
          n.consume(node) match {
            case Branch(innerBranchNodes) => innerBranchNodes
            case fj@ForkJoin(_) => List(fj)
          }
        } else {
          List(n)
        }
      }))
    }
  }

  def replaceNodeWith(node: Node, replacement: Node): (Node) => Node = {
    n => {
      if (n == node) {
        replacement
      } else {
        n
      }
    }
  }

  def extractComponents(node: ActionNode): ((List[Node], List[Node]), Node) => (List[Node], List[Node]) = {
    (accumulators, n) => {

      val onGoingBranch = accumulators._2

      if (
        (!onGoingBranch.head.canExecuteInParallelPartially(node) && !n.canExecuteInParallelPartially(node)) ||
        (onGoingBranch.head.canExecuteInParallelCompletely(node) && n.canExecuteInParallelCompletely(node)) ||
          ((!onGoingBranch.head.canExecuteInParallelCompletely(node) && !n.canExecuteInParallelCompletely(node)) &&
            onGoingBranch.head.canExecuteInParallelPartially(node) && n.canExecuteInParallelPartially(node))) {
        if (nodes.last == n) {
          (accumulators._1 :+ Branch.get(onGoingBranch :+ n), List())
        } else {
          (accumulators._1, onGoingBranch :+ n)
        }
      } else if (nodes.last == n) {
        (accumulators._1 :+ Branch.get(onGoingBranch) :+ n, List())
      } else {
        (accumulators._1 :+ Branch.get(onGoingBranch), List(n))
      }
    }
  }

  override def weight: Int = nodes.foldLeft(0)((acc, n) => acc + n.weight)

  override def representation: String = {
    nodes.map(_.representation).mkString("->")
  }
}

object Branch {
  def get(nodes: List[Node]) : Node = {
    if (nodes.size == 1) {
      nodes.head
    } else {
      new Branch(nodes)
    }
  }
}