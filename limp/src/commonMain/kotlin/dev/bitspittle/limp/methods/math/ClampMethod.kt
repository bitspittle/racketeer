package dev.bitspittle.limp.methods.math

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method

class ClampMethod : Method("clamp", 3) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val value = env.expectConvert<Int>(params[0])
        val low = env.expectConvert<Int>(params[1])
        val hi = env.expectConvert<Int>(params[2])
        return value.coerceIn(low, hi)
    }
}