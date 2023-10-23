package miniscribe

import rescala.default._
import miniscribe.data.{Force, Warband}
import scalatags.JsDom._
import scalatags.JsDom.all._

trait UIComponent:
  def render: HtmlTag

object UIComponent:
  type ID = String
  val toggleMenuEvt: Evt[ID] = Evt()
  // val toggled: Signal[Map[ID, Boolean]] =
  //   toggleMenuEvt.fold(Map.WithDefault(Map.empty[ID, Boolean], _ => false))(
  //     (acc, id) => acc.updated(id, !acc(id))
  //   )

  // things that change the UI state
  val toggled: Signal[Map[ID, Boolean]] =
    Events.foldAll(Map.WithDefault(Map.empty[ID, Boolean], _ => false)) { acc =>
      Seq(
        // toggle button presses
        toggleMenuEvt act2 (id => acc.updated(id, !acc(id))),
        // force removals
        armyEvents.deleteForce act2 (forceName => acc.updated(forceName, false))
      )
    }

case class ForceComponent(
    force: Force,
    heroOptions: Either[String, List[String]],
    toggled: Boolean = false
) extends UIComponent:
  def render: HtmlTag =
    div(
      `class` := "force",
      h2(force.name),
      force.warbands.map(w => WarbandComponent(w).render),
      div(
        a(
          span(
            s"${if toggled then "▼" else "▶"} " +
              s"${
                  if force.warbands.isEmpty then "add warband"
                  else "manage warbands"
                }"
          ),
          onclick := { () => UIComponent.toggleMenuEvt.fire(force.name) }
        ),
        " | ",
        a(
          "delete",
          onclick := { () =>
            miniscribe.armyEvents.deleteForce.fire(force.name)
          }
        )
      ),
      div(
        `class` := "heroOptions",
        display := s"${if toggled then "inherit" else "none"}",
        heroOptions match
          case Left(error) => div(error)
          case Right(options) =>
            ul(
              options.map(heroName =>
                li(
                  a(
                    heroName,
                    onclick := { () =>
                      armyEvents.addWarband.fire((force, heroName))
                    }
                  )
                )
              )
            )
      )
    )

case class WarbandComponent(
    warband: Warband
) extends UIComponent:
  def render: HtmlTag = div(
    h3(warband.hero.flatMap(_.model.map(_.name))),
    warband.troops.map(_.name)
  )
