package miniscribe

import rescala.default._
import miniscribe.data.{Force, Hero}
import scalatags.JsDom._
import scalatags.JsDom.all._
import org.scalajs.dom.HTMLDivElement
import rescala.extra.Tags._

trait UIComponent:
  def render: HtmlTag

case class ArmyComponent(forces: Signal[Seq[Force]]) extends UIComponent:
  // Maintain map of reactive sub components. Right now they are not deleted, just hidden when the
  // respective force disappears.
  val ForceComponents: Signal[Map[Force, ForceComponent]] =
    forces.changed.fold(forces.now.map(f => (f, ForceComponent(f))).toMap)(
      (current, forces) => {
        val newForces = forces.filter(!current.keySet.contains(_))
        current ++ newForces.map(f => (f, ForceComponent(f)))
      }
    )

  def render: HtmlTag =
    div(
      `class` := "army-overview",
      Signal {
        forces().map(ForceComponents()(_).render)
      }.asModifierL
    )

case class ForceComponent(force: Force, startToggled: Boolean = false)
    extends UIComponent:
  val toggled: Var[Boolean] = Var(false)
  def render: HtmlTag =
    div(
      `class` := "force",
      h2(force.name),
      div(
        a(
          Signal {
            span(s"${if toggled() then "▼" else "▶"} manage")
          }.asModifier,
          onclick := { () => toggled.transform(!_) }
        ),
        " | ",
        a(
          "delete",
          onclick := { () => miniscribe.ForceEvents.delete.fire(force.name) }
        )
      )
    )
