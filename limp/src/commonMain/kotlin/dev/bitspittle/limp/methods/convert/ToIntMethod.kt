package dev.bitspittle.limp.methods.convert

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method

/** Convert a string that represents an integer to an integer */
class ToIntMethod : Method("to-int", 1) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val a = env.expectConvert<String>(params[0])
        return a.toInt()
    }
}