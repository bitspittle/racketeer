package dev.bitspittle.racketeer.scripting.methods.card

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.model.card.Rarity
import kotlin.math.min
import kotlin.random.Random

/**
 * random-cards --filter ('Expr) (count: Int) -> (cards: List)
 *
 * Return a random sublist of all cards, with rarity taken into account. An optional filter can be passed in to whittle
 * down the list of cards that are being considered.
 */
class RandomCardsMethod(private val cards: List<CardTemplate>, private val rarities: List<Rarity>, private val random: () -> Random) : Method("random-cards", 1) {
    override suspend fun invoke(
        env: Environment, eval: Evaluator, params: List<Any>, options: Map<String, Any>, rest: List<Any>
    ): Any {
        val count = env.expectConvert<Int>(params[0])
        val filter = options["filter"]?.let {
            env.expectConvert<Expr>(it)
        }

        val inputList = mutableListOf<CardTemplate>()
        var totalCount = 0
        cards.forEach { card ->
            val shouldInclude = if (filter != null) {
                env.scoped { // Don't let values defined during the lambda escape
                    env.expectConvert(eval.extend(mapOf("\$card" to card)).evaluate(env, filter))
                }
            } else true

            if (shouldInclude) {
                totalCount++
                repeat(rarities[card.rarity].cardFrequency) { inputList.add(card) }
            }
        }

        val outputList = mutableListOf<CardTemplate>()

        var numCardsRemaining = min(count, totalCount)
        while (numCardsRemaining > 0) {
            val card = inputList.random(random())
            outputList.add(card)
            inputList.removeAll { it === card }
            --numCardsRemaining
        }

        return outputList
    }
}