package dev.bitspittle.racketeer.scripting.methods.game

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.methods.collection.ShuffleMethod
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.game.GameStateChange
import dev.bitspittle.racketeer.model.pile.Pile

/**
 * shuffle (MutableList)
 *
 * A game-state aware version of `shuffle!` Will delegate to the normal shuffle! method for non-game lists but will
 * intercept game-state-changing ones.
 */
class GameShuffleMethod(private val getGameState: () -> GameState) : Method("shuffle!", 1) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val maybePile = env.tryConvert<Pile>(params[0])

        val gameState = getGameState()
        if (maybePile == null) {
            val delegateMethod = ShuffleMethod { gameState.random() }
            delegateMethod.invoke(env, eval, params, options, rest)
        } else {
            getGameState().apply(GameStateChange.Shuffle(maybePile))
        }

        return Unit
    }
}