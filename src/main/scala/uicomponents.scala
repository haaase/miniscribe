package miniscribe

import rescala.default._
import miniscribe.data.{Force, Hero}
import scalatags.JsDom._
import scalatags.JsDom.all._
import org.scalajs.dom.HTMLDivElement

trait UIComponent:
  def render: HtmlTag

case object ForceComponent:
  val toggle = Evt[String]()
  // todo manage component state here:

case class ForceComponent(force: Force, toggled: Boolean = false)
    extends UIComponent:
  def render: HtmlTag = div(
    h2(force.name),
    div(
      a(
        s"${if this.toggled then "▼" else "▶"} manage",
        onclick := { () => ForceComponent.toggle.fire(force.name) }
      ),
      " | ",
      a(
        "delete",
        onclick := { () => miniscribe.ForceEvents.delete.fire(force.name) }
      )
    )
  )
