package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method

class ShuffledMethod : Method("shuffled", 1) {
    override suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val list = env.expectConvert<List<*>>(params[0])

        return list.shuffled(env.random)
    }
}