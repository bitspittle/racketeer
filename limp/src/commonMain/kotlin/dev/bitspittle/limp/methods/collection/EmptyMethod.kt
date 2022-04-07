package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method

class EmptyMethod : Method("empty?", 1) {
    override suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val list = env.expectConvert<List<Any>>(params[0])
        return list.isEmpty()
    }
}