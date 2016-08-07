package net.achalaggarwal.pdag.node

import net.achalaggarwal.pdag.Action
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, FlatSpec}

@RunWith(classOf[JUnitRunner])
class ActionNodeTest extends FlatSpec with Matchers {

  "Action Node" should "consume its dependent and execute it in serial after itself" in {
    val one: Action = new Action(1, 2)
    val two: Action = new Action(2, 2, one)
    val oneActionNode = new ActionNode(one)
    val twoActionNode = new ActionNode(two)

    oneActionNode.canExecuteInParallelCompletely(twoActionNode) should be(false)
    oneActionNode.canExecuteInSerial(twoActionNode) should be(true)
    oneActionNode.canExecuteInSerialFirst(twoActionNode) should be(false)
    oneActionNode.canExecuteInSerialLast(twoActionNode) should be(true)

    val expectedNode = new Branch(oneActionNode, twoActionNode)

    oneActionNode.consume(twoActionNode) should be(expectedNode)
  }

  "Action Node" should " consume its dependency and execute it in serial before itself" in {
    val one: Action = new Action(1, 2)
    val two: Action = new Action(2, 2, one)
    val oneActionNode = new ActionNode(one)
    val twoActionNode = new ActionNode(two)

    twoActionNode.canExecuteInParallelCompletely(oneActionNode) should be(false)
    twoActionNode.canExecuteInSerial(oneActionNode) should be(true)
    twoActionNode.canExecuteInSerialFirst(oneActionNode) should be(true)
    twoActionNode.canExecuteInSerialLast(oneActionNode) should be(false)

    val expectedNode = new Branch(oneActionNode, twoActionNode)

    oneActionNode.consume(twoActionNode) should be(expectedNode)
  }

  "Action Node" should " consume some independent action and execute it in parallel" in {
    val one: Action = new Action(1, 2)
    val two: Action = new Action(2, 2)
    val oneActionNode = new ActionNode(one)
    val twoActionNode = new ActionNode(two)

    twoActionNode.canExecuteInParallelCompletely(oneActionNode) should be(true)
    twoActionNode.canExecuteInSerialFirst(oneActionNode) should be(true)
    twoActionNode.canExecuteInSerialLast(oneActionNode) should be(true)
    twoActionNode.canExecuteInSerial(oneActionNode) should be(true)

    val expectedNode = new ForkJoin(Set(oneActionNode, twoActionNode))

    oneActionNode.consume(twoActionNode) should be(expectedNode)
  }
}
