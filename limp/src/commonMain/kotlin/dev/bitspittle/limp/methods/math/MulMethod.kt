package dev.bitspittle.limp.methods.math

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.Value

class MulMethod : Method("*", 2) {
    override fun invoke(env: Environment, params: List<Value>, optionals: Map<String, Value>, rest: List<Value>): Value {
        val a = env.expectConvert<Int>(params[0])
        val b = env.expectConvert<Int>(params[1])
        return Value(a * b)
    }
}