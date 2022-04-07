package dev.bitspittle.limp.methods.math

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method

class PowMethod : Method("^", 2) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val base = env.expectConvert<Int>(params[0])
        val exponent = env.expectConvert<Int>(params[1])

        if (exponent < 0) {
            throw IllegalArgumentException("Negative exponents are not supported.")
        }
        return (1..exponent).fold(1) { acc, _ -> acc * base }
    }
}