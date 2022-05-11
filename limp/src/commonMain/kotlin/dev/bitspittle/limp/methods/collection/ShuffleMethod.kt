package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import kotlin.random.Random

/**
 * shuffle! (MutableList)
 *
 * Take a mutable list and randomize it in place.
 */
class ShuffleMethod(private val random: () -> Random) : Method("shuffle!", 1) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val list = env.expectConvert<MutableList<*>>(params[0])
        list.shuffle(random())
        return Unit
    }
}