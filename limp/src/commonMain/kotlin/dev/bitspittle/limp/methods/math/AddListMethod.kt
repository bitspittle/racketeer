package dev.bitspittle.limp.methods.math

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.listTypeOf

class AddListMethod : Method("sum", 1) {
    override suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val ints = env.expectConvert<List<Int>>(params[0], listTypeOf())
        return ints.sum()
    }
}