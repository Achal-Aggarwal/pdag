package net.achalaggarwal.pdag.node

import net.achalaggarwal.pdag.Action
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class BranchTest extends FlatSpec with Matchers {
  "Branch(1,2) 1->2, 2->3 " should "1->2->3" in {
    val one: Action = new Action(1, 1)
    val two: Action = new Action(2, 2, one)
    val three: Action = new Action(3, 3, two)

    val oneActionNode = new ActionNode(one)
    val twoActionNode = new ActionNode(two)
    val threeActionNode = new ActionNode(three)
    val branch = new Branch(List(oneActionNode, twoActionNode))

    branch.canExecuteInSerialFirst(threeActionNode) should be(false)
    branch.canExecuteInSerialLast(threeActionNode) should be(true)
    branch.canExecuteInSerial(threeActionNode) should be(true)

    branch.canExecuteInParallelPartially(threeActionNode) should be(false)
    branch.canExecuteInParallelCompletely(threeActionNode) should be(false)

    branch.consume(threeActionNode) should be(new Branch(oneActionNode, twoActionNode, threeActionNode))
  }

  "Branch(1,2) 1->2, 3->1 " should "3->1->2" in {
    val three: Action = new Action(3, 3)
    val one: Action = new Action(1, 1, three)
    val two: Action = new Action(2, 2, one)


    val oneActionNode = new ActionNode(one)
    val twoActionNode = new ActionNode(two)
    val threeActionNode = new ActionNode(three)
    val branch = new Branch(List(oneActionNode, twoActionNode))

    branch.canExecuteInSerialFirst(threeActionNode) should be(true)
    branch.canExecuteInSerialLast(threeActionNode) should be(false)
    branch.canExecuteInSerial(threeActionNode) should be(true)

    branch.canExecuteInParallelPartially(threeActionNode) should be(false)
    branch.canExecuteInParallelCompletely(threeActionNode) should be(false)

    branch.consume(threeActionNode) should be(new Branch(threeActionNode, oneActionNode, twoActionNode))
  }

  "Branch(1,2) 1->2, 1->3, 3->2 " should "1->3->2" in {
    val one: Action = new Action(1, 1)
    val three: Action = new Action(3, 3, one)
    val two: Action = new Action(2, 2, three, one)


    val oneActionNode = new ActionNode(one)
    val twoActionNode = new ActionNode(two)
    val threeActionNode = new ActionNode(three)
    val branch = new Branch(List(oneActionNode, twoActionNode))

    branch.canExecuteInSerialFirst(threeActionNode) should be(false)
    branch.canExecuteInSerialLast(threeActionNode) should be(false)
    branch.canExecuteInSerial(threeActionNode) should be(true)

    branch.canExecuteInParallelPartially(threeActionNode) should be(false)
    branch.canExecuteInParallelCompletely(threeActionNode) should be(false)

    branch.consume(threeActionNode) should be(new Branch(oneActionNode, threeActionNode, twoActionNode))
  }

  "Branch(1,2) 1->2, 3->2 " should "(1|3)->2" in {
    val one: Action = new Action(1, 1)
    val three: Action = new Action(3, 3)
    val two: Action = new Action(2, 2, one, three)

    val oneActionNode = new ActionNode(one)
    val twoActionNode = new ActionNode(two)
    val threeActionNode = new ActionNode(three)
    val branch = new Branch(List(oneActionNode, twoActionNode))

    branch.canExecuteInSerialFirst(threeActionNode) should be(true)
    branch.canExecuteInSerialLast(threeActionNode) should be(false)
    branch.canExecuteInSerial(threeActionNode) should be(true)

    branch.canExecuteInParallelPartially(threeActionNode) should be(true)
    branch.canExecuteInParallelCompletely(threeActionNode) should be(false)

    branch.consume(threeActionNode) should be(new Branch(new ForkJoin(Set(oneActionNode, threeActionNode)), twoActionNode))
  }

  "Branch(1,2) 1->2, 1->3 " should "1->(2|3)" in {
    val one: Action = new Action(1, 1)
    val three: Action = new Action(3, 3, one)
    val two: Action = new Action(2, 2, one)

    val oneActionNode = new ActionNode(one)
    val twoActionNode = new ActionNode(two)
    val threeActionNode = new ActionNode(three)
    val branch = new Branch(List(oneActionNode, twoActionNode))

    branch.canExecuteInSerialFirst(threeActionNode) should be(false)
    branch.canExecuteInSerialLast(threeActionNode) should be(true)
    branch.canExecuteInSerial(threeActionNode) should be(true)

    branch.canExecuteInParallelPartially(threeActionNode) should be(true)
    branch.canExecuteInParallelCompletely(threeActionNode) should be(false)

    branch.consume(threeActionNode) should be(new Branch(oneActionNode, new ForkJoin(Set(threeActionNode, twoActionNode))))
  }

  "Branch(1,2) 1->2, 3 " should "(1->2)|3" in {
    val one: Action = new Action(1, 1)
    val three: Action = new Action(3, 3)
    val two: Action = new Action(2, 2, one)

    val oneActionNode = new ActionNode(one)
    val twoActionNode = new ActionNode(two)
    val threeActionNode = new ActionNode(three)
    val branch = new Branch(List(oneActionNode, twoActionNode))

    branch.canExecuteInSerialFirst(threeActionNode) should be(true)
    branch.canExecuteInSerialLast(threeActionNode) should be(true)
    branch.canExecuteInSerial(threeActionNode) should be(true)

    branch.canExecuteInParallelPartially(threeActionNode) should be(true)
    branch.canExecuteInParallelCompletely(threeActionNode) should be(true)

    branch.consume(threeActionNode) should be(new ForkJoin(Set(threeActionNode, new Branch(oneActionNode, twoActionNode))))
  }

  "Branch(1,2) 1->2, 2->3, 3->4, 3->5, 4->5 " should "1->2->3->4->5" in {
    val one: Action = new Action(1, 1)
    val two: Action = new Action(2, 2, one)
    val three: Action = new Action(3, 3, two)
    val four: Action = new Action(4, 4, three)
    val five: Action = new Action(5, 5, three, four)

    val oneActionNode = new ActionNode(one)
    val twoActionNode = new ActionNode(two)
    val threeActionNode = new ActionNode(three)
    val fourActionNode = new ActionNode(four)
    val fiveActionNode = new ActionNode(five)
    val branch = new Branch(List(oneActionNode, twoActionNode))
      .consume(threeActionNode)
      .consume(fiveActionNode)

    branch.canExecuteInSerialFirst(fourActionNode) should be(false)
    branch.canExecuteInSerialLast(fourActionNode) should be(false)
    branch.canExecuteInSerial(fourActionNode) should be(true)

    branch.canExecuteInParallelCompletely(fourActionNode) should be(false)
    branch.canExecuteInParallelCompletely(fourActionNode) should be(false)

    branch.consume(fourActionNode) should be(new Branch(oneActionNode, twoActionNode, threeActionNode, fourActionNode, fiveActionNode))
  }
}
