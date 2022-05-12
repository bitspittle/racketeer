package dev.bitspittle.racketeer.scripting.types

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.racketeer.model.action.ActionQueue
import dev.bitspittle.racketeer.model.action.ExprCache
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardEnqueuer
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.scripting.utils.setValuesFrom

class CardEnqueuerImpl(
    private val env: Environment,
    private val exprCache: ExprCache,
    private val actionQueue: ActionQueue,
) : CardEnqueuer {
    private fun enqueueActions(gameState: GameState, card: Card, actions: List<String>) {
        val evaluator = Evaluator()
        actions.forEach { action ->
            actionQueue.enqueue {
                env.scoped {
                    env.setValuesFrom(gameState)
                    env.setValuesFrom(card)

                    evaluator.evaluate(env, exprCache.parse(action))
                }
            }
        }
    }

    override fun enqueueInitActions(gameState: GameState, card: Card) = enqueueActions(gameState, card, card.template.initActions)
    override fun enqueuePlayActions(gameState: GameState, card: Card) = enqueueActions(gameState, card, card.template.playActions)
    override fun enqueuePassiveActions(gameState: GameState, card: Card) = enqueueActions(gameState, card, card.template.passiveActions)
}
