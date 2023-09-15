package miniscribe
import scala.concurrent.ExecutionContext.Implicits.global
import rescala.default._
import scala.util.Failure
import scala.util.Success
import miniscribe.data.{AppState, Force}
import java.util.Base64
import org.scalajs.dom
import org.scalajs.dom.URLSearchParams
import scala.util.Try
import org.scalajs.dom.PopStateEvent
import scala.xml.{Document => XMLDocument}
import rescala.default

sealed trait EventTypes
case object ForwardBackward extends EventTypes
sealed trait ForceEvent extends EventTypes:
  def force: String
case class Add(force: String) extends ForceEvent
case class Delete(force: String) extends ForceEvent

object ForceEvents:
  val add = Evt[String]()
  val delete = Evt[String]()

object NavigationEvents:
  val forwardBackward = Evt[Unit]()

class Controller:
  // initialize based on URL parameters
  private def parseState() =
    val params = URLSearchParams(dom.window.location.search)
    if params.has("state") then
      Try(
        AppState
          .parseFrom(Base64.getDecoder.decode(params.get("state")))
      ).getOrElse(AppState())
    else AppState()

  // app state can be changed through these events
  private val addAct = ForceEvents.add.act[AppState] { f =>
    current.copy(forces = current.forces :+ Force(f, List()))
  }
  private val delAct = ForceEvents.delete.act[AppState] { f =>
    current.copy(forces = current.forces.filter(_._1 != f))
  }
  private val forwBackwAct = NavigationEvents.forwardBackward.act[AppState] {
    _ =>
      parseState() // whenever we detect a forward/backward event, simply parse state from proto
  }
  val state: Signal[AppState] =
    Fold(parseState())(addAct, delAct, forwBackwAct)

  // fetch army options
  private val armyIndex: Var[Either[String, Map[String, XMLDocument]]] =
    Var(
      Left("Fetching army index...")
    )
  DataBackend.buildArmyIndex().onComplete {
    case Success(value) => armyIndex.set(Right(value))
    case Failure(exception) =>
      armyIndex.set(Left(s"Failed to acquire army index: $exception"))
  }

  val availableForceOptions: Signal[Either[String, Seq[String]]] =
    Signal {
      val forceNames = state().forces.map(_.name).toSet
      armyIndex().map(_.keys.toList.filter(!forceNames.contains(_)))
    }

  // all hero options
  private val allHeroOptions
      : Signal[Either[String, Map[String, List[String]]]] =
    Signal {
      // armyIndex().map(_.mapValues(DataBackend.getHeroOptions(_)).toMap)
      Left("Failed to display available heroes.")
    }

  val heroOptions: Signal[Map[Force, Either[String, List[String]]]] = Signal {
    state().forces
      .map(f =>
        val myOpt = for
          allOpt <- allHeroOptions()
          taken = f.warbands.map(_.hero.get.model.get.name)
        yield allOpt(f.name).filter(!taken.contains(_))
        (f, myOpt)
      )
      .toMap
  }

  // update history when AppState changes but not on forward/backward events
  private val lastEvent =
    ((ForceEvents.add || ForceEvents.delete).map(_ =>
      "force"
    ) || NavigationEvents.forwardBackward.map(_ => "fb"))
      .latest()
  Signal { (lastEvent(), state()) }.observe {
    case ("fb", _) => ()
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

  // listen to history navigation events
  dom.window.addEventListener(
    "popstate",
    { (e: PopStateEvent) =>
      NavigationEvents.forwardBackward.fire()
    }
  )
