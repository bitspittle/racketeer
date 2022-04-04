package dev.bitspittle.limp.methods.compare

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.Value

class EqualsMethod : Method("=", 2) {
    override suspend fun invoke(env: Environment, params: List<Value>, options: Map<String, Value>, rest: List<Value>): Value {
        val a = env.expectConvert<Any>(params[0])
        val b = env.expectConvert<Any>(params[1])
        return Value(a == b)
    }
}