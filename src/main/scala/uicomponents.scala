package miniscribe

import rescala.default._
import miniscribe.data.{Force}
import scalatags.JsDom._
import scalatags.JsDom.all._
import rescala.extra.Tags._

trait UIComponent:
  def render: HtmlTag

class ArmyComponent(forces: Signal[Seq[Force]]) extends UIComponent:
  // Maintain map of reactive sub components. Right now they are not deleted, just hidden when the
  // respective force disappears.
  val forceComponents: Signal[Map[String, ForceComponent]] =
    forces.changed.fold(Map.empty[String, ForceComponent])(
      (currentComponents, currentForces) =>
        val missingComponents =
          currentForces.filter(f => !currentComponents.keySet.contains(f.name))
        currentComponents ++ missingComponents.map(f =>
          (
            f.name,
            ForceComponent(Signal.dynamic { forces().find(_.name == f.name) })
          )
        )
    )

  val visibleComponents: Signal[List[ForceComponent]] = Signal {
    forceComponents()
      .filter((name, _) => forces().map(_.name).contains(name))
      .values
      .toList
  }

  forces.observe(f => println(s"Forces: $f"))
  forceComponents.observe(c => println(s"ForceComponents: ${c.keys}"))
  // visibleComponents.observe(c =>
  //   println(s"VisibleComponents: ${c.map(_.getName)}")
  // )

  def render: HtmlTag =
    div(
      `class` := "army-overview",
      Signal {
        visibleComponents().map(_.render)
      }.asModifierL
    )

class ForceComponent(
    force: Signal[Option[Force]]
    // startToggled: Boolean = false
) extends UIComponent:
  // val toggled: Var[Boolean] = Var(startToggled)
  // def getName: String = force.now.map(_.name).getOrElse("No force specified!")
  def render: HtmlTag =
    div(
      `class` := "force",
      Signal {
        force() match
          case None => span("No force specified.")
          case Some(f) =>
            div(
              h2(f.name),
              div(
                // a(
                //   span(s"${if toggled() then "▼" else "▶"} manage"),
                //   onclick := { () => toggled.transform(!_) }
                // ),
                " | ",
                a(
                  "delete",
                  onclick := { () =>
                    miniscribe.ForceEvents.delete.fire(f.name)
                  }
                )
              )
            )
      }.asModifier
    )
