package miniscribe

import rescala.default.*
import rescala.extra.Tags._
import scalatags.JsDom.all._
import org.scalajs.dom.html.{Element, Div}
import org.scalajs.dom.UIEvent
import rescala.default
import scalatags.JsDom._
import miniscribe.data.{Force, Hero}
import miniscribe.data.given

class toggleMenu(label: String, content: Signal[TypedTag[Element]]):
  val toggle: Evt[String] = Evt()
  println(s"menu $label created")
  private val visible: Signal[Boolean] =
    this.toggle.fold(true)((vis, _) => !vis)
  private val toggleButton =
    Events.fromCallback[UIEvent](cb =>
      Signal.dynamic {
        a(
          s"${if this.visible() then "▼" else "▶"} $label",
          onclick := cb
        )
      }.asModifier
    )
  this.toggleButton.event.observe(_ => toggle.fire(label))
  this.toggleButton.event.observe(_ => println(s"$label pressed"))
  this.toggle.observe(_ => println(s"visibility of $label toggled"))
  this.visible.observe(_ =>
    println(s"visible of $label changed. Is now ${visible.now}")
  )
  val show = Signal {
    span(
      Seq(
        toggleButton.data,
        div(
          display := (if this.visible() then "inherit" else "none"),
          content()
        )
      )
    )
  }
class View(controller: Controller):
  val appState = controller.state

  trait ToHTML[A]:
    extension (a: A) def toHTML: Tag

  given ToHTML[Hero] with
    extension (h: Hero)
      def toHTML: TypedTag[Div] =
        div(h.model.map(_.name).getOrElse("ERR: No Name Defined"))

  given ToHTML[Force] with
    extension (f: Force)
      def toHTML =
        div(
          h2(`class` := "force", f.name)
        )

  /** Begin UI elements */
  val title: Signal[StringFrag] = Signal {
    StringFrag(appState().forces match
      case Nil => "Please select a force"
      case l =>
        val name = l.length match
          case 1 => s"${l.head.name} Army"
          case _ =>
            s"${l.take(l.length - 1).map(_.name).mkString(", ")}, and ${l.last.name} Alliance"
        s"$name (${appState().forces.map(_.cost).sum}P)"
    )
  }

  val warbandMenu: Force => Signal[TypedTag[Element]] =
    _ =>
      Signal {
        div(toggleMenu("add warband", Signal { p("TODO") }).show())
      }

  def addForceButton(army: String): TypedTag[Element] =
    val cb =
      Events.fromCallback[UIEvent](cb => a(army, onclick := cb))
    cb.event.observe(_ => miniscribe.ArmyEvents.addForce.fire(army))
    cb.event.observe(_ => forcesMenu.toggle.fire("addForce"))
    return cb.data

  def removeForceButton(army: String): TypedTag[Element] =
    val cb =
      Events.fromCallback[UIEvent](cb => a("delete", onclick := cb))
    cb.event.observe(_ => miniscribe.ArmyEvents.deleteForce.fire(army))
    return cb.data

  val forcesMenu = toggleMenu(
    "add force",
    Signal {
      controller.availableForceOptions() match
        case Left(message) => p(message)
        case Right(forceOptions) =>
          ul(forceOptions.sorted.map(name => li(addForceButton(name))))
    }
  )

  // render function
  def getContent(): HtmlTag =
    body(
      h1(title.asModifier),
      div(Signal {
        appState().forces.map(f =>
          ForceComponent(
            f,
            controller.heroOptions()(f),
            UIComponent.toggled()(f.name)
          ).render
        )
      }.asModifierL),
      forcesMenu.show.asModifier
    )
