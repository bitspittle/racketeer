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
 * blueprint-build! (Blueprint)
 *
 * Build the target blueprint, converting it into a building now.
 *
 * It is an error to try to build a blueprint you already have a building for.
 */
class BlueprintBuildMethod(private val getGameState: () -> GameState) : Method("blueprint-build!", 1) {
    override suspend fun invoke(
        env: Environment, eval: Evaluator, params: List<Any>, options: Map<String, Any>, rest: List<Any>
    ): Any {
        val blueprint = env.expectConvert<Blueprint>(params[0])
        val state = getGameState()
        val blueprintIndex = state.blueprints.indexOf(blueprint).takeIf { it >= 0 }
            ?: error("You cannot build the blueprint \"${blueprint.name}\" as you don't own it.")
        state.apply(GameStateChange.Build(blueprintIndex))
        return Unit
    }
}