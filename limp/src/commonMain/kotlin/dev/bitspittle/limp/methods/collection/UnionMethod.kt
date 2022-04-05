package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method

class UnionMethod : Method("union", 0, consumeRest = true) {
    override suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val listOfLists = rest.map { value -> env.expectConvert<List<*>>(value) }
        return listOfLists.flatten()
    }
}