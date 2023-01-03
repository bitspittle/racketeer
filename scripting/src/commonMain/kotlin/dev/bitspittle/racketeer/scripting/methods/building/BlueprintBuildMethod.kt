package dev.bitspittle.racketeer.scripting.methods.building

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.game.GameStateChange

/**
 * blueprint-build! (Blueprint)
 *
 * Build the target blueprint, converting it into a building now.
 *
 * It is an error to try to build a blueprint you already have a building for.
 */
class BlueprintBuildMethod(private val addGameChange: suspend (GameStateChange) -> Unit) : Method("blueprint-build!", 1) {
    override suspend fun invoke(
        env: Environment, eval: Evaluator, params: List<Any>, options: Map<String, Any>, rest: List<Any>
    ): Any {
        val blueprint = env.expectConvert<Blueprint>(params[0])
        addGameChange(GameStateChange.Build(blueprint, free = true))
        return Unit
    }
}