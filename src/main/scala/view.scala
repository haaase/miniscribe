package miniscribe.view

import rescala.default._
import rescala.extra.Tags._
import miniscribe.model._
import scalatags.JsDom.all._
import scalatags.JsDom.TypedTag
import org.scalajs.dom.html.{Element, Div, Input, LI}
import typings.node.nodeStrings.arm

trait ToHTML[A]:
  extension (a: A) def toHTML: TypedTag[Element]

given ToHTML[Hero] with
  extension (h: Hero)
    def toHTML: TypedTag[Div] =
      div(h.name)

def getContent: TypedTag[Div] =
  val army: Var[List[Hero]] = Var(List())
  army.transform(
    _ :+
      Hero(
        "Bernd",
        tier = Tier.Fortitude,
        baseCost = 200,
        equipment = List()
      )
  )
  army.transform(
    _ :+ Hero("Claudia", tier = Tier.Legend, baseCost = 300, equipment = List())
  )

  div(Signal { army().map(_.toHTML) }.asModifierL)
