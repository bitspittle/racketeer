package dev.bitspittle.racketeer.scripting.methods.building

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.utils.toEnum
import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.building.BlueprintProperty

/**
 * blueprint-get (Blueprint) ('Property) -> Any
 *
 * Query the [BlueprintProperty] from a target building, returning its value
 */
class BlueprintGetMethod : Method("blueprint-get", 2) {
    override suspend fun invoke(
        env: Environment, eval: Evaluator, params: List<Any>, options: Map<String, Any>, rest: List<Any>
    ): Any {
        val blueprint = env.expectConvert<Blueprint>(params[0])
        val identifier = env.expectConvert<Expr.Identifier>(params[1])
        val property = identifier.toEnum(BlueprintProperty.values())

        return when (property) {
            BlueprintProperty.BUILD_COST_CASH -> blueprint.buildCost.cash
            BlueprintProperty.BUILD_COST_INFLUENCE -> blueprint.buildCost.influence
            BlueprintProperty.NAME -> blueprint.name
            BlueprintProperty.RARITY -> blueprint.rarity
            BlueprintProperty.VP -> blueprint.vp
        }
    }
}