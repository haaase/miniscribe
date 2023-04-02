package miniscribe
import scala.concurrent.ExecutionContext.Implicits.global
import rescala.default._
import scala.util.Failure
import scala.util.Success
import cats.conversions.all
import miniscribe.data.{AppState, Force}
import miniscribe.data.given
import java.util.Base64
import java.nio.charset.StandardCharsets
import org.scalajs.dom
import org.scalajs.dom.URLSearchParams
import scala.util.Try

object Events:
  val addForceEvent = Evt[String]()
  val deleteForceEvent = Evt[String]()

class Controller:
  // initialize based on URL parameters
  val params = URLSearchParams(dom.window.location.search)
  val initialState =
    if params.has("state") then
      Try(
        AppState
          .parseFrom(Base64.getDecoder.decode(params.get("state")))
      ).getOrElse(AppState())
    else AppState()

  private val forceEvents =
    Events.addForceEvent.map(Right(_)) || Events.deleteForceEvent.map(Left(_))
  val state: Signal[AppState] =
    forceEvents.fold(initialState) {
      case (state, Right(newForce)) =>
        state.copy(forces = state.forces :+ Force(newForce, List()))
      case (state, Left(removeForce)) =>
        state.copy(forces = state.forces.filter(_._1 != removeForce))
    }

  // fetch army options
  private val allForceOptions: Var[Either[String, Seq[String]]] = Var(
    Left("Fetching army index...")
  )
  private val armyIndex =
    DataBackend.buildArmyIndex()
  armyIndex.onComplete {
    case Success(value) => allForceOptions.set(Right(value.keys.toSeq))
    case Failure(exception) =>
      allForceOptions.set(Left(s"Failed to acquire army index: $exception"))
  }
  val availableForceOptions =
    Signal {
      val forceNames = state().forces.map(_.name).toSet
      allForceOptions().map(_.filter(!forceNames.contains(_)))
    }

  private val proto: Signal[String] = Signal {
    Base64.getEncoder.encodeToString(
      data.AppState(forces = state().forces).toByteArray
    )
  }

  proto.observe(p =>
    if !(p.isEmpty()) then
      dom.window.history.pushState(null, "title", s"?state=$p")
    else
      dom.window.history.pushState(
        null,
        "title",
        s"${dom.window.location.href.split("\\?").head}"
      )
  )
