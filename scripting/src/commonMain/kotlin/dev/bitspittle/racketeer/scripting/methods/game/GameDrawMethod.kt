package dev.bitspittle.racketeer.scripting.methods.game

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.game.GameStateChange

class GameDrawMethod(private val getGameState: () -> GameState, private val addGameChange: suspend (GameStateChange) -> Unit) : Method("game-draw!", 1) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val count = env.expectConvert<Int>(params[0])
        val state = getGameState()
        addGameChange(GameStateChange.Draw(count))
        return Unit
    }
}