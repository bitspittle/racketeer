package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import kotlin.random.Random

/**
 * repeat (Any) (count: Int) -> List
 *
 * Repeat some item some number of times
 */
class RepeatMethod : Method("repeat", 2) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val item = params[0]
        val count = env.expectConvert<Int>(params[1])
        // Required for consistency between JS and JVM
        if (count < 0) throw IllegalArgumentException("repeat must be a non-negative number")

        return List(count) { item }
    }
}