package dev.bitspittle.limp.methods.convert

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method

/** Convert any value to its string representation */
class ToStringMethod : Method("to-string", 1) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val a = env.expectConvert<Any>(params[0])
        return a.toString()
    }
}