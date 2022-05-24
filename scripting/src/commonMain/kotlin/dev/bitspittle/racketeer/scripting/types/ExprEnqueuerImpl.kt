package dev.bitspittle.racketeer.scripting.types

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.racketeer.model.action.ActionQueue
import dev.bitspittle.racketeer.model.action.ExprCache
import dev.bitspittle.racketeer.model.game.ExprEnqueuer
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.scripting.utils.setValuesFrom

class ExprEnqueuerImpl(
    private val env: Environment,
    private val exprCache: ExprCache,
    private val actionQueue: ActionQueue,
) : ExprEnqueuer {
    override fun enqueue(gameState: GameState, code: String) {
        val evaluator = Evaluator()
        actionQueue.enqueue {
            env.scoped {
                env.setValuesFrom(gameState)
                evaluator.evaluate(env, exprCache.parse(code))
            }

        }
    }
}
