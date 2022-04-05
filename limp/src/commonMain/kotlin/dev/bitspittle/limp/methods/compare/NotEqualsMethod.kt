package dev.bitspittle.limp.methods.compare

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method

class NotEqualsMethod : Method("!=", 2) {
    override suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val a = env.expectConvert<Any>(params[0])
        val b = env.expectConvert<Any>(params[1])
        return a != b
    }
}