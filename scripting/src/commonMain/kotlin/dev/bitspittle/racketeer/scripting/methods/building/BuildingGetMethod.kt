package dev.bitspittle.racketeer.scripting.methods.building

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.utils.toEnum
import dev.bitspittle.racketeer.model.building.Building
import dev.bitspittle.racketeer.model.building.BuildingProperty
import dev.bitspittle.racketeer.model.building.vpTotal
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.vpTotal
import dev.bitspittle.racketeer.scripting.converters.CardTemplateToCardConverter
import dev.bitspittle.racketeer.model.card.CardProperty

/**
 * building-get (Building) ('Property) -> Any
 *
 * Query the [BuildingProperty] from a target building, returning its value
 */
class BuildingGetMethod : Method("building-get", 2) {
    override suspend fun invoke(
        env: Environment, eval: Evaluator, params: List<Any>, options: Map<String, Any>, rest: List<Any>
    ): Any {
        val building = env.expectConvert<Building>(params[0])
        val identifier = env.expectConvert<Expr.Identifier>(params[1])
        val property = identifier.toEnum(BuildingProperty.values())

        return when (property) {
            BuildingProperty.BLUEPRINT -> building.blueprint
            BuildingProperty.COUNTER -> building.counter
            BuildingProperty.ID -> building.id.toString() // Strings rae easier to work with / pass around
            BuildingProperty.IS_ACTIVATED -> building.isActivated
            BuildingProperty.NAME -> building.blueprint.name
            BuildingProperty.RARITY -> building.blueprint.rarity
            BuildingProperty.VP -> building.vpBase
            BuildingProperty.VP_PASSIVE -> building.vpPassive
            BuildingProperty.VP_TOTAL -> building.vpTotal
        }
    }
}