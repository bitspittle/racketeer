package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import kotlin.random.Random

/**
 * shuffled (List) -> List
 *
 * Return a shuffled copy of the list.
 */
class ShuffledMethod(private val random: () -> Random) : Method("shuffled", 1) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val list = env.expectConvert<List<Any>>(params[0])
        return list.shuffled(random())
    }
}