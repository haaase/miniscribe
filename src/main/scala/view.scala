package miniscribe

import rescala.default._
import rescala.extra.Tags._
import miniscribe.model._
import scalatags.JsDom.all._
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
  val title: Signal[Tag] = Signal {
    h1(
      appState().forces match
        case Nil => "Please select a force"
        case l =>
          val name = l match
            case x :: Nil => s"${x.name} Army"
            case l =>
              s"${l.take(l.length - 1).map(_.name).mkString(", ")}, and ${l.last.name} Alliance"
          s"$name (${appState().points}P)"
    )
  }

  trait ToHTML[A]:
    extension (a: A) def toHTML: Tag

  given ToHTML[Hero] with
    extension (h: Hero)
      def toHTML: TypedTag[Div] =
        div(h.name)

  given ToHTML[Force] with
    extension (f: Force)
      def toHTML =
        div(
          h2(`class` := "force", f.name),
          removeArmyButton(f.name)
        )

  // private val addForce: CBResult[UIEvent, TypedTag[Button]] =
  //   Events.fromCallback[UIEvent](cb => button("add force", onclick := cb))

  def addArmyButton(army: String): TypedTag[Element] =
    val cb =
      Events.fromCallback[UIEvent](cb => a(army, onclick := cb))
    cb.event.observe(_ => miniscribe.Events.addForceEvent.fire(army))
    cb.event.observe(_ => toggleForcesMenuEvent.fire())
    return cb.data

  def removeArmyButton(army: String): TypedTag[Element] =
    val cb =
      Events.fromCallback[UIEvent](cb => a("delete", onclick := cb))
    cb.event.observe(_ => miniscribe.Events.deleteForceEvent.fire(army))
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
    toggleForcesMenuEvent.fold(false)((vis, _) => !vis)
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
      display := (if forcesMenuVisible() then "inherit" else "none"),
      controller.availableForceOptions() match
        case Left(message) => p(message)
        case Right(forceOptions) =>
          ul(forceOptions.sorted.map(name => li(addArmyButton(name))))
    )
  }
  val navbar = ul(
    `class` := "menu",
    toggleForcesButton.data
  )

  // render function
  def getContent(): HtmlTag =
    body(Signal {
      div(
        title(),
        div(
          controller.state().forces.map(_.toHTML)
        ),
        navbar,
        forcesMenu()
      )
    }.asModifier)
