package dev.bitspittle.racketeer.scripting.methods.card

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.racketeer.model.action.ActionQueue
import dev.bitspittle.racketeer.model.card.CardEnqueuer
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.model.game.GameState

/**
 * card-instantiate (CardTemplate) -> Card
 *
 * Create a card from a template AND calculate any passive VP calculations if it has any. This is useful for creating
 * VP cards from templates with actual, up-to-date VP values before showing them to the user.
 */
class CardInstantiateMethod(private val actionQueue: ActionQueue, private val cardEnqueuer: CardEnqueuer, private val getGameState: () -> GameState) : Method("card-instantiate", 1) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val template = env.expectConvert<CardTemplate>(params[0])
        val card = template.instantiate()
        cardEnqueuer.enqueuePassiveActions(getGameState(), card)
        actionQueue.runEnqueuedActions()

        return card
    }
}