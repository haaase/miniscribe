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
import org.scalajs.dom.PopStateEvent

sealed trait EventTypes
case object ForwardBackward extends EventTypes
sealed trait ForceEvent extends EventTypes:
  def force: String
case class Add(force: String) extends ForceEvent
case class Delete(force: String) extends ForceEvent

object Events:
  val addForceEvent = Evt[String]()
  val deleteForceEvent = Evt[String]()
  val forwardBackwardEvent = Evt[Unit]()

class Controller:
  // initialize based on URL parameters
  def parseState() =
    val params = URLSearchParams(dom.window.location.search)
    if params.has("state") then
      Try(
        AppState
          .parseFrom(Base64.getDecoder.decode(params.get("state")))
      ).getOrElse(AppState())
    else AppState()

  // app state can be changed through these events
  val stateEvents = (Events.addForceEvent.map(Add(_)) ||
    Events.deleteForceEvent.map(Delete(_)) ||
    Events.forwardBackwardEvent.map(_ => ForwardBackward))

  val state: Signal[AppState] = stateEvents
    .fold(parseState()) {
      case (state, Add(force)) =>
        state.copy(forces = state.forces :+ Force(force, List()))
      case (state, Delete(force)) =>
        state.copy(forces = state.forces.filter(_._1 != force))
      case (state, ForwardBackward) =>
        parseState()
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

  // update history when AppState changes but not on forward/backward events
  val lastEvent = stateEvents.latest()
  Signal { (lastEvent(), state()) }.observe {
    case (ForwardBackward, _) => ()
    case (_, s) =>
      val p = Base64.getEncoder.encodeToString(
        data.AppState(forces = s.forces).toByteArray
      )
      if !(p.isEmpty()) then
        dom.window.history.pushState(p, "title", s"?state=$p")
      else
        dom.window.history.pushState(
          "",
          "title",
          s"${dom.window.location.href.split("\\?").head}"
        )
  }

  dom.window.addEventListener(
    "popstate",
    { (e: PopStateEvent) =>
      // println(
      //   s"popstate: ${e.state}.\n window.location:${dom.window.location}"
      // );
      Events.forwardBackwardEvent.fire()
    }
  )
