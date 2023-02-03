package miniscribe
import scala.concurrent.ExecutionContext.Implicits.global
import rescala.default._
import miniscribe.model.Force
import scala.util.Failure
import scala.util.Success

case class AppState(forces: List[Force] = List())

object Events:
  val addForceEvent = Evt[String]()

class Controller:
  val state: Signal[AppState] =
    Events.addForceEvent.fold(AppState())((state, newForce) =>
      state.copy(forces = state.forces :+ Force(newForce, List()))
    )
  val forceOptions: Var[Seq[String]] = Var(List("Fetching army index..."))

  val dataIndex = databackend.getArmies().map(_.map(_._1))

  dataIndex.onComplete {
    case Success(value) => forceOptions.set(value)
    case Failure(exception) =>
      forceOptions.set(List(s"Failed to acquire army index: $exception"))
  }
