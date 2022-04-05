package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method

/**
 * shuffle (MutableList)
 *
 * Take a mutable list and randomize it in place.
 */
class ShuffleMethod : Method("shuffle", 1) {
    override suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val list = env.expectConvert<MutableList<*>>(params[0])
        list.shuffle(env.random)
        return list
    }
}