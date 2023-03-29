package miniscribe.model

transparent trait HasCost:
  def cost: Int

case class Force(name: String, warbands: Seq[Warband]) extends HasCost:
  def cost: Int = warbands.map(_.cost).sum

case class Warband(hero: Hero, troopEntries: Seq[Troop]) extends HasCost:
  def cost: Int = hero.cost + troopEntries.map(_.cost).sum

case class Hero(
    name: String,
    tier: Tier,
    isLeader: Boolean = false,
    baseCost: Int,
    equipment: Seq[Equipment]
) extends Model

case class Troop(name: String, baseCost: Int, equipment: Seq[Equipment])
    extends Model

trait Model extends HasCost:
  def name: String
  def equipment: Seq[Equipment]
  def baseCost: Int
  def cost: Int =
    baseCost + equipment.filter(_.selected).map(_.cost).sum

case class Equipment(cost: Int, selected: Boolean = false)

enum Tier:
  case Legend, Valour, Fortitude, Minor, Independent
