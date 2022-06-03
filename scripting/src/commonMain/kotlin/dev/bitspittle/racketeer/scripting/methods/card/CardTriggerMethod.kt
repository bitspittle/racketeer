package dev.bitspittle.racketeer.scripting.methods.card

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardEnqueuer
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.scripting.converters.CardTemplateToCardConverter

/**
 * card-trigger! (Card)
 *
 * Enqueue the target card's actions, effectively running it (without actually playing it) after this card finishes
 * running. You can trigger a card in any pile, even the graveyard or the store if you wanted to.
 *
 * See also: card-play!
 */
class CardTriggerMethod(private val cardEnqueuer: CardEnqueuer, private val getGameState: () -> GameState) : Method("card-trigger!", 1) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val card = env.scoped {
            env.addConverter(CardTemplateToCardConverter())
            env.expectConvert<Card>(params[0])
        }

        cardEnqueuer.enqueuePlayActions(getGameState(), card)
        return Unit
    }
}