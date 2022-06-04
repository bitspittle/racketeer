package dev.bitspittle.racketeer.scripting.methods.building

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.card.Rarity
import kotlin.random.Random

/**
 * random-blueprints (count: Int) -> (blueprints: List)
 *
 * Return a random sublist of all blueprints, with rarity taken into account.
 */
class RandomBlueprintsMethod(private val blueprints: List<Blueprint>, private val rarities: List<Rarity>, private val random: () -> Random) : Method("random-blueprints", 1) {
    override suspend fun invoke(
        env: Environment, eval: Evaluator, params: List<Any>, options: Map<String, Any>, rest: List<Any>
    ): Any {
        val count = env.expectConvert<Int>(params[0])

        val outputList = mutableListOf<Blueprint>()
        val inputList = mutableListOf<Blueprint>()
        blueprints.forEach { blueprint ->
            repeat(rarities[blueprint.rarity].blueprintFrequency) { inputList.add(blueprint) }
        }

        var numBlueprintsRemaining = count
        while (numBlueprintsRemaining > 0) {
            val bp = inputList.random(random())
            outputList.add(bp)
            inputList.removeAll { it === bp }
            --numBlueprintsRemaining
        }

        return outputList.also { it.sort() }
    }
}