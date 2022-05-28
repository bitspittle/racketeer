package dev.bitspittle.racketeer.scripting.methods.game

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.game.GameStateChange

/**
 * game-data-set! (String) (String)
 *
 * Add some arbitrary text data into the current game state which can later be fetched by the same string key name.
 */
class GameDataSetMethod(private val getGameState: () -> GameState) : Method("game-data-set!", 2) {
    override suspend fun invoke(env: Environment, eval: Evaluator, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val key = env.expectConvert<String>(params[0])
        val value = env.expectConvert<String>(params[1])

        getGameState().apply(GameStateChange.SetGameData(key, value))
        return Unit
    }
}