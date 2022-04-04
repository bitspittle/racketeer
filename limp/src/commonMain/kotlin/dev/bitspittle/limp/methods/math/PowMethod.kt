package dev.bitspittle.limp.methods.math

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.Value

class PowMethod : Method("^", 2) {
    override suspend fun invoke(env: Environment, params: List<Value>, options: Map<String, Value>, rest: List<Value>): Value {
        val base  = env.expectConvert<Int>(params[0])
        val exponent = env.expectConvert<Int>(params[1])
        return Value((1..exponent).fold(1) { acc, _ -> acc * base })
    }
}