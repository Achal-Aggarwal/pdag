package net.achalaggarwal.pdag

import net.achalaggarwal.pdag.node.ActionNode
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class IntegrationTest extends FlatSpec with Matchers {

  "Integration Test" should "correctly produce a graph" in {
    val one = new Action(1, 6)
    val two = new Action(2, 1)
    val three = new Action(3, 2, one)
    val four = new Action(4, 8, one, two)
    val five = new Action(5, 9, two)
    val six = new Action(6, 4, two)
    val seven = new Action(7, 5, four, five)
    val eight = new Action(8, 10, six)
    val nine = new Action(9, 7, three)
    val ten = new Action(10, 3, nine, four, eight)

    val actions = List(one, two, three, four, five, six, seven, eight, nine, ten)

    val node =
      actions
        .tail
        .sortBy(-_.weight)
        .foldLeft(new ActionNode(actions.head).asInstanceOf[Node])((acc, n) => acc.consume(new ActionNode(n)))

    node.representation should be("2->(6->8|(5|1->(4|3->9))->7)->10")
    node.weight should be(24)
  }
}