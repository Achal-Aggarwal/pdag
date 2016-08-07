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
      ForkJoin.get(node, this)
    } else if (canExecuteInParallelPartially(node)) {
      val components = nodes.tail.foldLeft((List.empty[Node], List(nodes.head)))(extractComponents(node))._1

      val slowestParallelBranch =
        components
        .zipWithIndex
        .filter(_._1.canExecuteInParallelPartially(node))
        .sortBy(-_._1.weight)
        .head

      Branch.get(
        components.updated(
          index = slowestParallelBranch._2,
          elem = slowestParallelBranch._1.consume(node)
        )
      )
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

      Branch.get(nodes.flatMap(
        consumedByInnerNode(
          nodeToConsume = node,
          consumedByNode = serialExecutableNode
        )
      ))
    }
  }

  def consumedByInnerNode(nodeToConsume: ActionNode, consumedByNode: Node): (Node) => List[Node] = {
    n => {
      if (n == consumedByNode) {
        n.consume(nodeToConsume) match {
          case Branch(innerBranchNodes) => innerBranchNodes
          case fj@ForkJoin(_) => List(fj)
        }
      } else {
        List(n)
      }
    }
  }

  def extractComponents(node: ActionNode): ((List[Node], List[Node]), Node) => (List[Node], List[Node]) = {
    (accumulators, n) => {

      val onGoingBranch = accumulators._2

      val a = onGoingBranch.head.canExecuteInParallelCompletely(node)
      val b = onGoingBranch.head.canExecuteInParallelPartially(node)

      val c = n.canExecuteInParallelPartially(node)
      val d = n.canExecuteInParallelCompletely(node)

      val isBothLastAndCurrentSerial = !b && !c
      val isBothLastAndCurrentCompletelyParallel = a && d
      val isBothLastAndCurrentJustPartiallyParallel = (!a && !d) && b && c

      if (isBothLastAndCurrentSerial ||
          isBothLastAndCurrentCompletelyParallel ||
          isBothLastAndCurrentJustPartiallyParallel) {
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

  def get(nodes: Node*) : Node = {
    get(nodes.toList)
  }
}