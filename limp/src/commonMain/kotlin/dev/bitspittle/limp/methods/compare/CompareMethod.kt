package dev.bitspittle.limp.methods.compare

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method

/**
 * A method which compares two values and returns an integer which indicates their sorted relationship.
 *
 * A negative value means the first item is smaller than the second, a positive means larger, and a 0 means equal.
 */
class CompareMethod : Method("compare", 2) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val a = env.expectConvert<Comparable<Any>>(params[0])
        val b = env.expectConvert<Comparable<Any>>(params[1])
        return a.compareTo(b)
    }
}