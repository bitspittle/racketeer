package dev.bitspittle.lispish.methods.math

import dev.bitspittle.lispish.Environment
import dev.bitspittle.lispish.Method
import dev.bitspittle.lispish.Value

class AddMethod : Method("+", 2) {
    override fun invoke(env: Environment, params: List<Value>): Value {
        val a = env.expectConvert<Int>(params[0])
        val b = env.expectConvert<Int>(params[1])
        return Value(a + b)
    }
}