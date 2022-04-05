package dev.bitspittle.limp.methods.math

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method

class AddListMethod : Method("sum", 1) {
    override suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val ints = env.expectConvert<List<Int>>(params[0])
        return ints.sum()
    }
}