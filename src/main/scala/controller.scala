package miniscribe
import scala.concurrent.ExecutionContext.Implicits.global
import rescala.default._
import miniscribe.model.Force
import scala.util.Failure
import scala.util.Success
import cats.conversions.all

case class AppState(forces: List[Force] = List()):
  def points: Int = forces.map(_.cost).sum

object Events:
  val addForceEvent = Evt[String]()
  val deleteForceEvent = Evt[String]()

class Controller:
  private val forceEvents =
    Events.addForceEvent.map(Right(_)) || Events.deleteForceEvent.map(Left(_))
  val state: Signal[AppState] =
    forceEvents.fold(AppState()) {
      case (state, Right(newForce)) =>
        state.copy(forces = state.forces :+ Force(newForce, List()))
      case (state, Left(removeForce)) =>
        state.copy(forces = state.forces.filter(_._1 != removeForce))
    }

  // fetch army options
  private val allForceOptions: Var[Either[String, Seq[String]]] = Var(
    Left("Fetching army index...")
  )
  private val dataIndex = DataBackend.getArmyIndex().map(_.map(_._1))
  dataIndex.onComplete {
    case Success(value) => allForceOptions.set(Right(value))
    case Failure(exception) =>
      allForceOptions.set(Left(s"Failed to acquire army index: $exception"))
  }
  val availableForceOptions =
    Signal {
      val forceNames = state().forces.map(_.name).toSet
      allForceOptions().map(_.filter(!forceNames.contains(_)))
    }
