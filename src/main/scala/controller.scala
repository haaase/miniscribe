package miniscribe

import rescala.default._
import miniscribe.model.Force

case class AppState(forces: List[Force] = List())

class Controller:
  val state: Signal[AppState] =
    val aForceEvent = view.addForceEvent.map(_ => "a force")
    val events = aForceEvent || view.addForceEvent2
    events.fold(AppState())((state, newForce) =>
      state.copy(forces = state.forces :+ Force(newForce, List()))
    )
