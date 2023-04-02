package miniscribe.data

trait HasCost[T]:
  extension (x: T) def cost: Int

given HasCost[Model] with
  extension (x: Model)
    def cost: Int =
      x.baseCost + x.equipment.filter(_.selected).map(_.cost).sum

given HasCost[Hero] with
  extension (x: Hero)
    def cost: Int =
      x.model.get.cost

given HasCost[Warband] with
  extension (x: Warband)
    def cost: Int = x.hero.get.cost + x.troops.map(_.cost).sum

given HasCost[Force] with
  extension (x: Force) def cost: Int = x.warbands.map(_.cost).sum
// transparent trait HasCost:
//   def cost: Int

// case class Force(name: String, warbands: Seq[Warband]) extends HasCost:
//   def cost: Int = warbands.map(_.cost).sum

// case class Warband(hero: Hero, troopEntries: Seq[Troop]) extends HasCost:
//   def cost: Int = hero.cost + troopEntries.map(_.cost).sum

// case class Hero(
//     name: String,
//     tier: Tier,
//     isLeader: Boolean = false,
//     baseCost: Int,
//     equipment: Seq[Equipment]
// ) extends Model

// case class Troop(name: String, baseCost: Int, equipment: Seq[Equipment])
//     extends Model

// trait Model extends HasCost:
//   def name: String
//   def equipment: Seq[Equipment]
//   def baseCost: Int
//   def cost: Int =
//     baseCost + equipment.filter(_.selected).map(_.cost).sum

// case class Equipment(name: String, cost: Int, selected: Boolean = false)

// enum Tier:
//   case Legend, Valour, Fortitude, Minor, Independent
