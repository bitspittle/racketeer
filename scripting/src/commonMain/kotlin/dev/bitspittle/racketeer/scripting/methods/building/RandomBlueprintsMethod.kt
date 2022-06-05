package dev.bitspittle.racketeer.scripting.methods.building

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.model.card.Rarity
import kotlin.math.min
import kotlin.random.Random

/**
 * random-blueprints --filter ('Expr) (count: Int) -> (blueprints: List)
 *
 * Return a random sublist of all blueprints, with rarity taken into account. An optional filter can be passed in to
 * whittle down the list of cards that are being considered.
 */
class RandomBlueprintsMethod(private val blueprints: List<Blueprint>, private val rarities: List<Rarity>, private val random: () -> Random) : Method("random-blueprints", 1) {
    override suspend fun invoke(
        env: Environment, eval: Evaluator, params: List<Any>, options: Map<String, Any>, rest: List<Any>
    ): Any {
        val count = env.expectConvert<Int>(params[0])
        val filter = options["filter"]?.let {
            env.expectConvert<Expr>(it)
        }

        val inputList = mutableListOf<Blueprint>()
        var totalCount = 0
        blueprints.forEach { blueprint ->
            val shouldInclude = if (filter != null) {
                env.scoped { // Don't let values defined during the lambda escape
                    env.expectConvert(eval.extend(mapOf("\$blueprint" to blueprint)).evaluate(env, filter))
                }
            } else true

            if (shouldInclude) {
                totalCount++
                repeat(rarities[blueprint.rarity].blueprintFrequency) { inputList.add(blueprint) }
            }
        }

        val outputList = mutableListOf<Blueprint>()
        var numBlueprintsRemaining = min(count, totalCount)
        while (numBlueprintsRemaining > 0) {
            val bp = inputList.random(random())
            outputList.add(bp)
            inputList.removeAll { it === bp }
            --numBlueprintsRemaining
        }

        return outputList
    }
}