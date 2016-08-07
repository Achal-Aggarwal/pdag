package net.achalaggarwal.pdag.node

import net.achalaggarwal.pdag.Action
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, FlatSpec}

@RunWith(classOf[JUnitRunner])
class ForkJoinTest extends FlatSpec with Matchers {
  "ForkJoin(1,2) 3" should "produce 1|2|3" in {
    val one: Action = new Action(1, 1)
    val two: Action = new Action(2, 2)
    val three: Action = new Action(3, 3)

    val oneActionNode = new ActionNode(one)
    val twoActionNode = new ActionNode(two)
    val threeActionNode = new ActionNode(three)

    val fj = new ForkJoin(Set(oneActionNode, twoActionNode))

    fj.canExecuteInSerialFirst(threeActionNode) should be(true)
    fj.canExecuteInSerialLast(threeActionNode) should be(true)
    fj.canExecuteInSerial(threeActionNode) should be(true)
    fj.canExecuteInParallelCompletely(threeActionNode) should be(true)

    fj.consume(threeActionNode) should be(new ForkJoin(Set(oneActionNode, twoActionNode, threeActionNode)))
  }

  "ForkJoin(1,2) 1->3, 2->3" should "produce (1|2)->3" in {
    val one: Action = new Action(1, 1)
    val two: Action = new Action(2, 2)
    val three: Action = new Action(3, 3, one, two)

    val oneActionNode = new ActionNode(one)
    val twoActionNode = new ActionNode(two)
    val threeActionNode = new ActionNode(three)

    val fj = new ForkJoin(Set(oneActionNode, twoActionNode))

    fj.canExecuteInSerialFirst(threeActionNode) should be(false)
    fj.canExecuteInSerialLast(threeActionNode) should be(true)
    fj.canExecuteInSerial(threeActionNode) should be(true)
    fj.canExecuteInParallelCompletely(threeActionNode) should be(false)

    fj.consume(threeActionNode) should be(new Branch(new ForkJoin(Set(oneActionNode, twoActionNode)), threeActionNode))
  }

  "ForkJoin(1,2) 3->1, 3->2" should "produce 3->(1|2)" in {
    val three: Action = new Action(3, 3)
    val one: Action = new Action(1, 1, three)
    val two: Action = new Action(2, 2, three)

    val oneActionNode = new ActionNode(one)
    val twoActionNode = new ActionNode(two)
    val threeActionNode = new ActionNode(three)

    val fj = new ForkJoin(Set(oneActionNode, twoActionNode))

    fj.canExecuteInSerialFirst(threeActionNode) should be(true)
    fj.canExecuteInSerialLast(threeActionNode) should be(false)
    fj.canExecuteInSerial(threeActionNode) should be(true)
    fj.canExecuteInParallelCompletely(threeActionNode) should be(false)

    fj.consume(threeActionNode) should be(new Branch(threeActionNode, new ForkJoin(Set(oneActionNode, twoActionNode))))
  }

  "ForkJoin(1,2) 1->3" should "produce (1->3|2)" in {
    val one: Action = new Action(1, 1)
    val two: Action = new Action(2, 2)
    val three: Action = new Action(3, 3, one)

    val oneActionNode = new ActionNode(one)
    val twoActionNode = new ActionNode(two)
    val threeActionNode = new ActionNode(three)

    val fj = new ForkJoin(Set(oneActionNode, twoActionNode))

    fj.canExecuteInSerialFirst(threeActionNode) should be(false)
    fj.canExecuteInSerialLast(threeActionNode) should be(true)
    fj.canExecuteInSerial(threeActionNode) should be(true)
    fj.canExecuteInParallelCompletely(threeActionNode) should be(false)

    fj.consume(threeActionNode) should be(new ForkJoin(Set(twoActionNode, new Branch(oneActionNode, threeActionNode))))
  }

  "ForkJoin(1->3,2) 1->3, 4->3" should "produce ((1|4)->3|2)" in {
    val one: Action = new Action(1, 1)
    val two: Action = new Action(2, 2)
    val four: Action = new Action(4, 4)
    val three: Action = new Action(3, 3, one, four)

    val oneActionNode = new ActionNode(one)
    val twoActionNode = new ActionNode(two)
    val threeActionNode = new ActionNode(three)
    val fourActionNode = new ActionNode(four)

    val fj = new ForkJoin(Set(oneActionNode, twoActionNode)).consume(threeActionNode)

    fj.canExecuteInSerialFirst(fourActionNode) should be(true)
    fj.canExecuteInSerialLast(fourActionNode) should be(false)
    fj.canExecuteInSerial(fourActionNode) should be(true)
    fj.canExecuteInParallelCompletely(fourActionNode) should be(false)

    fj.consume(fourActionNode) should be(new ForkJoin(Set(twoActionNode, new Branch(ForkJoin(Set(fourActionNode, oneActionNode)), threeActionNode))))
  }

  "ForkJoin(1->3,2) 1->3, 4->3, 1->4" should "produce (1->4->3|2)" in {
    val one: Action = new Action(1, 1)
    val two: Action = new Action(2, 2)
    val four: Action = new Action(4, 4, one)
    val three: Action = new Action(3, 3, one, four)

    val oneActionNode = new ActionNode(one)
    val twoActionNode = new ActionNode(two)
    val threeActionNode = new ActionNode(three)
    val fourActionNode = new ActionNode(four)

    val fj = new ForkJoin(Set(oneActionNode, twoActionNode)).consume(threeActionNode)

    fj.canExecuteInSerialFirst(fourActionNode) should be(false)
    fj.canExecuteInSerialLast(fourActionNode) should be(false)
    fj.canExecuteInSerial(fourActionNode) should be(false)
    fj.canExecuteInParallelPartially(fourActionNode) should be(true)
    fj.canExecuteInParallelCompletely(fourActionNode) should be(false)

    fj.consume(fourActionNode) should be(new ForkJoin(Set(twoActionNode, new Branch(oneActionNode, fourActionNode, threeActionNode))))
  }
}
