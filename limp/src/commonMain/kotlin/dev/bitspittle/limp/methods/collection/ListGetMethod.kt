package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.Value

/**
 * Take a list and a 0-based index and return the item at that index.
 */
class ListGetMethod : Method("list-get", 2) {
    override suspend fun invoke(env: Environment, params: List<Value>, options: Map<String, Value>, rest: List<Value>): Value {
        val list = env.expectConvert<List<Any>>(params[0])
        val index = env.expectConvert<Int>(params[1])

        return Value(list[index])
    }
}