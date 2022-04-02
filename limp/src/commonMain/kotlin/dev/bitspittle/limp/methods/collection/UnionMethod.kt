package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.Value
import kotlin.math.max
import kotlin.math.min

class UnionMethod : Method("union", 0, consumeRest = true) {
    override fun invoke(env: Environment, params: List<Value>, options: Map<String, Value>, rest: List<Value>): Value {
        val listOfLists = rest.map { value -> env.expectConvert<List<*>>(value) }
        return Value(listOfLists.flatten())
    }
}