package dev.bitspittle.racketeer.scripting.methods.card

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.game.MutableGameState

class CardPileMethod(private val getGameState: () -> MutableGameState) : Method("card-pile", 1) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val card = env.expectConvert<Card>(params[0])
        return getGameState().pileFor(card) ?: throw IllegalArgumentException("The requested card does not seem to exist in any pile")
    }
}