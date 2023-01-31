package miniscribe.view

import rescala.default._
import rescala.extra.Tags._
import miniscribe.model._
import scalatags.JsDom.all._
import scalatags.JsDom.TypedTag
import scalatags.JsDom.{TypedTag}
import org.scalajs.dom.html.{Element, Div, Input, LI}
import typings.node.nodeStrings.arm
import miniscribe.AppState
import org.scalajs.dom.UIEvent
import org.scalajs.dom.html.Button
import rescala.default
import rescala.default.Events.CBResult

trait ToHTML[A]:
  extension (a: A) def toHTML: TypedTag[Element]

given ToHTML[Hero] with
  extension (h: Hero)
    def toHTML: TypedTag[Div] =
      div(h.name)

given ToHTML[Force] with
  extension (f: Force)
    def toHTML: TypedTag[Div] =
      div(f.name)

// UI elements
private val addForce: CBResult[UIEvent, TypedTag[Button]] =
  Events.fromCallback[UIEvent](cb => button("add force", onclick := cb))
val addForceEvent = addForce.event

def buttonHandler(
    army: String,
    tag: TypedTag[Button]
): CBResult[String, TypedTag[Button]] = {
  val handler =
    Events.fromCallback[UIEvent](cb => tag(army, onclick := cb))

  val button: TypedTag[Button] = handler.data

  val handlerEvent =
    handler.event.map { (e: UIEvent) =>
      army
    }

  new CBResult(handlerEvent, button)
}

val addForceButton = buttonHandler("b force", button())
val addForceEvent2 = addForceButton.event

// render function
def getContent(state: Signal[AppState]): TypedTag[Element] =
  // val navbar = ul(
  //   a("add Force")
  // )

  // val army: Var[List[Hero]] = Var(List())
  // army.transform(
  //   _ :+
  //     Hero(
  //       "Bernd",
  //       tier = Tier.Fortitude,
  //       baseCost = 200,
  //       equipment = List()
  //     )
  // )
  // army.transform(
  //   _ :+ Hero(
  //     "Claudia",
  //     tier = Tier.Legend,
  //     baseCost = 300,
  //     equipment = List()
  //   )
  // )

  // div(Signal { army().map(_.toHTML) }.asModifierL)
  div(
    Signal {
      state().forces.map(_.toHTML)
    }.asModifierL,
    addForce.data,
    addForceButton.data
  )
