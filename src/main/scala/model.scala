package miniscribe.model

case class Force(name: String, warbands: Seq[Warband])

case class Warband(hero: Hero, troopEntries: Seq[Troop])

case class Hero(
    name: String,
    tier: Tier,
    isLeader: Boolean,
    baseCost: Int,
    equipment: Seq[Equipment]
) extends Model

case class Troop(name: String, baseCost: Int, equipment: Seq[Equipment])
    extends Model

trait Model:
  val name: String
  val equipment: Seq[Equipment]
  val baseCost: Int
  def getCost(): Int =
    baseCost + equipment.filter(_.selected).map(_.cost).sum

case class Equipment(cost: Int, selected: Boolean)

enum Tier:
  case Legend, Valour, Fortitude, Minor, Independent
