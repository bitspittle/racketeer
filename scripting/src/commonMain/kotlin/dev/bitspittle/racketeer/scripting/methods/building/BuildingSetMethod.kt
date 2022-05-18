package dev.bitspittle.racketeer.scripting.methods.building

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.converters.ItemToSingletonListConverter
import dev.bitspittle.limp.converters.ValueToExprConverter
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.listTypeOf
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.utils.toEnum
import dev.bitspittle.racketeer.model.building.Building
import dev.bitspittle.racketeer.model.building.BuildingProperty
import dev.bitspittle.racketeer.model.card.CardProperty
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.game.GameStateChange

/**
 * building-set (Building) ('Property) ('Expr)
 *
 * Set a writable [BuildingProperty] for a target building, using an expression that returns a new value (which is
 * passed in a parameter called `$it` which is bound to the property's current value).
 */
class BuildingSetMethod(private val getGameState: () -> GameState) : Method("building-set!", 3) {
    override suspend fun invoke(env: Environment, eval: Evaluator, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val buildings = env.scoped {
            env.addConverter(ItemToSingletonListConverter(Building::class))
            env.expectConvert<List<Building>>(params[0], listTypeOf())
        }
        val identifier = env.expectConvert<Expr.Identifier>(params[1])
        val property = identifier.toEnum(BuildingProperty.values())

        val setExpr = env.scoped {
            env.addConverter(ValueToExprConverter(Int::class))
            env.expectConvert<Expr>(params[2])
        }

        buildings.forEach { building ->
            val currValue = when (property) {
                BuildingProperty.COUNTER -> building.counter
                BuildingProperty.VP -> building.vpBase
                BuildingProperty.VP_PASSIVE -> building.vpPassive

                BuildingProperty.BLUEPRINT,
                BuildingProperty.IS_ACTIVATED,
                BuildingProperty.ID,
                BuildingProperty.NAME,
                BuildingProperty.RARITY,
                BuildingProperty.VP_TOTAL -> throw EvaluationException(
                    identifier.ctx, "Cannot set this building's property as it is read-only."
                )
            }

            val newValue = env.scoped { // Don't let values defined during the lambda escape
                val evaluator = eval.extend(mapOf("\$it" to currValue))
                env.expectConvert<Int>(evaluator.evaluate(env, setExpr))
            }

            getGameState().apply(GameStateChange.AddBuildingAmount(property, building, newValue - currValue))
        }

        return Unit
    }
}