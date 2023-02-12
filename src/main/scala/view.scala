package miniscribe

import rescala.default._
import rescala.extra.Tags._
import miniscribe.model._
import scalatags.JsDom.all._
import scalatags.JsDom.TypedTag
import scalatags.JsDom.{TypedTag}
import org.scalajs.dom.html.{Element, Div, Heading, Input, LI}
import miniscribe.AppState
import org.scalajs.dom.UIEvent
import org.scalajs.dom.html.Button
import rescala.default
import rescala.default.Events.CBResult
import miniscribe.Controller
import scalatags.JsDom._
import scala.xml.Elem

class View(controller: Controller):
  val appState = controller.state

  // ui elements
  val heading: Signal[TypedTag[Element]] = Signal {
    header(
      h1(
        appState().forces match
          case Nil => "Please select a force"
          case l   => s"${l.map(_.name).mkString(" and ")} Army"
      )
    )
  }

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

  // private val addForce: CBResult[UIEvent, TypedTag[Button]] =
  //   Events.fromCallback[UIEvent](cb => button("add force", onclick := cb))

  def addArmyButton(army: String): TypedTag[Element] =
    val cb =
      Events.fromCallback[UIEvent](cb => a(army, onclick := cb))
    cb.event.observe(_ => miniscribe.Events.addForceEvent.fire(army))
    return cb.data

  // def buildArmyButton(
  //     army: String,
  //     tag: TypedTag[Button]
  // ): CBResult[String, TypedTag[Button]] = {
  //   val handler =
  //     Events.fromCallback[UIEvent](cb => tag(army, onclick := cb))
  //   val button: TypedTag[Button] = handler.data

  //   new CBResult(miniscribe.Controller.addForceEvent, button)
  // }
  val toggleForcesMenuEvent: Evt[Unit] = Evt()
  val forcesMenuVisible: Signal[Boolean] =
    toggleForcesMenuEvent.fold(false)((acc, _) => !acc)
  val toggleForcesButton =
    Events.fromCallback[UIEvent](cb =>
      Signal {
        a(
          s"${if forcesMenuVisible() then "▼" else "▶"} add force",
          onclick := cb
        )
      }.asModifier
    )
  val observer =
    toggleForcesButton.event.observe(_ => toggleForcesMenuEvent.fire())

  val forcesMenu: Signal[TypedTag[Element]] = Signal {
    div(
      visibility := (if forcesMenuVisible() then "inherit" else "hidden"),
      controller.forceOptions() match
        case Left(message) => p(message)
        case Right(forceOptions) =>
          ul(forceOptions.map(name => li(addArmyButton(name))))
    )
  }
  val navbar = ul(
    `class` := "menu",
    toggleForcesButton.data
  )

  // render function
  def getContent(): TypedTag[Element] =
    div(
      heading.asModifier,
      div(Signal {
        controller.state().forces.map(_.toHTML)
      }.asModifierL),
      navbar,
      forcesMenu.asModifier
    )
