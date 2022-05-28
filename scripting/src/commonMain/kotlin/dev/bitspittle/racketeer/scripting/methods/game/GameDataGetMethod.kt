package dev.bitspittle.racketeer.scripting.methods.game

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.racketeer.model.game.GameState

/**
 * game-data-get (String) -> Data
 *
 * Returns data associated with the passed in key.
 *
 * This method will crash if there is no data associated with the key. You may want to use
 * `game-data-is-set?` first.
 */
class GameDataGetMethod(private val getGameState: () -> GameState) : Method("game-data-get", 1) {
    override suspend fun invoke(env: Environment, eval: Evaluator, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val key = env.expectConvert<String>(params[0])

        return getGameState().data[key] ?: error("No game data associated with key \"$key\"")
    }
}