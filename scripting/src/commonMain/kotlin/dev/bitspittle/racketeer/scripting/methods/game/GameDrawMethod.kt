package dev.bitspittle.racketeer.scripting.methods.game

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.game.GameStateDelta

class GameDrawMethod(private val getGameState: () -> GameState) : Method("game-draw!", 1) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val count = env.expectConvert<Int>(params[0])
        val state = getGameState()
        state.apply(GameStateDelta.Draw(count))
        return Unit
    }
}