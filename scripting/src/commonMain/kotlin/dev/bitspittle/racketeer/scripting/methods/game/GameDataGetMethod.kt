package dev.bitspittle.racketeer.scripting.methods.game

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.serialization.DataValue
import dev.bitspittle.racketeer.model.serialization.asAny

/**
 * game-data-get --default (Data) (key: String) -> Data
 *
 * Returns data associated with the passed in key.
 *
 * If no data associated with the key, this method will crash unless a --default value is provided. You can consider
 * checking with `game-data-is-set?` first if no default makes sense.
 */
class GameDataGetMethod(private val getGameState: () -> GameState) : Method("game-data-get", 1) {
    override suspend fun invoke(env: Environment, eval: Evaluator, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val key = env.expectConvert<String>(params[0])
        val default = options["default"]?.let { DataValue.of(it).asAny }

        return getGameState().data[key]?.asAny ?: default ?: error("No game data associated with key \"$key\" and no default provided")
    }
}