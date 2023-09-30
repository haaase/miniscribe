package miniscribe

import rescala.default._
import miniscribe.data.{Force}
import scalatags.JsDom._
import scalatags.JsDom.all._
import rescala.extra.Tags._

trait UIComponent:
  def render: HtmlTag

object UIComponent:
  type ID = String
  val triggerMenuEvt: Evt[ID] = Evt()
  val triggered: Signal[Map[ID, Boolean]] =
    triggerMenuEvt.fold(Map.WithDefault(Map.empty[ID, Boolean], _ => false))(
      (acc, id) => acc.updated(id, !acc(id))
    )

class ForceComponent(
    force: Force
) extends UIComponent:
  def render: HtmlTag =
    div(
      `class` := "force",
      h2(force.name),
      div(
        a(
          Signal {
            span(
              s"${if UIComponent.triggered()(force.name) then "▼" else "▶"} " +
                s"${
                    if force.warbands.isEmpty then "add warband"
                    else "manage warbands"
                  }"
            )
          }.asModifier,
          onclick := { () => UIComponent.triggerMenuEvt.fire(force.name) }
        ),
        " | ",
        a(
          "delete",
          onclick := { () =>
            miniscribe.ForceEvents.delete.fire(force.name)
          }
        )
      )
    )
