package dev.bitspittle.racketeer.scripting.methods.building

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.utils.toEnum
import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.building.BlueprintProperty
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.game.GameStateChange

/**
 * blueprint-own! (Blueprint)
 *
 * Take ownership of the target blueprint. This will throw an error if the user already owned that blueprint (even if
 * they built it so it's no longer in their blueprint list).
 */
class BlueprintOwnMethod(private val getGameState: () -> GameState) : Method("blueprint-own!", 1) {
    override suspend fun invoke(
        env: Environment, eval: Evaluator, params: List<Any>, options: Map<String, Any>, rest: List<Any>
    ): Any {
        val blueprint = env.expectConvert<Blueprint>(params[0])
        getGameState().apply(GameStateChange.AddBlueprint(blueprint))
        return Unit
    }
}