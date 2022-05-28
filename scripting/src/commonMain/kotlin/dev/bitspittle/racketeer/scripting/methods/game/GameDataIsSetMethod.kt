package dev.bitspittle.racketeer.scripting.methods.game

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.racketeer.model.game.GameState

/**
 * game-data-is-set? (String) -> Boolean
 *
 * Returns true if there's data set for this game with the specified key, false otherwise.
 */
class GameDataIsSetMethod(private val getGameState: () -> GameState) : Method("game-data-is-set?", 1) {
    override suspend fun invoke(env: Environment, eval: Evaluator, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val key = env.expectConvert<String>(params[0])
        return getGameState().data.containsKey(key)
    }
}