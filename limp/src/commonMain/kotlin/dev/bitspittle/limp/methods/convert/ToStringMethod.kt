package dev.bitspittle.limp.methods.convert

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.Value

/** Convert any value to its string representation */
class ToStringMethod : Method("to-string", 1) {
    override suspend fun invoke(env: Environment, params: List<Value>, options: Map<String, Value>, rest: List<Value>): Value {
        val a = env.expectConvert<Any>(params[0])
        return Value(a.toString())
    }
}