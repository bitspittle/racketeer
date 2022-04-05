package dev.bitspittle.limp.methods.math

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import kotlin.math.max

class MaxMethod : Method("max", 2) {
    override suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val a = env.expectConvert<Int>(params[0])
        val b = env.expectConvert<Int>(params[1])
        return max(a, b)
    }
}