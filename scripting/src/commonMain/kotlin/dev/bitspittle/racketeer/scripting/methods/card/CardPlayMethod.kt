package dev.bitspittle.racketeer.scripting.methods.card

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.game.GameStateChange

/**
 * card-play! (Card)
 *
 * Play the target card programmatically instead of having the user do it manually. The card MUST be in your hand,
 * and when this method is finished running, it will have moved to the street (unless the card itself has a modifier or
 * play actions which intercept this behavior).
 *
 * See also: card-trigger!
 */
class CardPlayMethod(private val addGameChange: suspend (GameStateChange) -> Unit) : Method("card-play!", 1) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val card = env.expectConvert<Card>(params[0])
        addGameChange(GameStateChange.Play(card))
        return Unit
    }
}