package miniscribe
import scala.concurrent.ExecutionContext.Implicits.global
import rescala.default._
import scala.util.Failure
import scala.util.Success
import miniscribe.data.{AppState, Force, Hero, Model, Warband}
import java.util.Base64
import org.scalajs.dom
import org.scalajs.dom.URLSearchParams
import scala.util.Try
import org.scalajs.dom.PopStateEvent
import scala.xml.{Document => XMLDocument}
import rescala.default

// ==== World events ====
object ArmyEvents:
  val addForce = Evt[String]() // forceName
  val deleteForce = Evt[String]() // forceName
  val addWarband = Evt[(Force, String)]() // force, heroName

object NavigationEvents:
  val forwardBackward = Evt[Unit]()
// ======================

class Controller:
  // ===== Build and modify app state =====
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
  private val addForceAct = ArmyEvents.addForce.act[AppState] { f =>
    current.copy(forces = current.forces :+ Force(f, List()))
  }
  private val delForceAct = ArmyEvents.deleteForce.act[AppState] { f =>
    current.copy(forces = current.forces.filter(_._1 != f))
  }
  private val addWarbandAct = ArmyEvents.addWarband.act[AppState] { (f, h) =>
    current.copy(forces = current.forces.map {
      case g @ Force(name, warbands, _) if f == g =>
        val newHero = Hero(model = Some(Model(name = h)))
        Force(name, warbands = (warbands.:+(Warband(hero = Some(newHero)))))
      case g => g
    })
  }
  private val forwBackwAct = NavigationEvents.forwardBackward.act[AppState] {
    _ =>
      parseState() // whenever we detect a forward/backward event, simply parse state from proto
  }
  val state: Signal[AppState] =
    Fold(parseState())(addForceAct, delForceAct, addWarbandAct, forwBackwAct)

  state.observe(s => println(s"state changed: $s"))
  // =========================

  // ==== Derived values ====
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
      armyIndex().map(
        _.view.mapValues(DataBackend.getHeroOptions(_).toList).toMap
      )
      // Left("Failed to display available heroes.")
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
  // ===============================

  // ===== Browser history API a.k.a. handle forward/backward events =======
  // update history when AppState changes but not on forward/backward events
  private val lastEvent =
    (state.changed.map(_ => "armyChange") || NavigationEvents.forwardBackward
      .map(_ => "fb"))
      .latest()
  Signal { (lastEvent(), state()) }.observe {
    case ("fb", _) => ()
    case (_, state) =>
      val p = Base64.getEncoder.encodeToString(
        state.toByteArray
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
  // ==================================================================
