package dev.bitspittle.limp.methods.math

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.Value
import kotlin.math.min

class MinMethod : Method("min", 2) {
    override suspend fun invoke(env: Environment, params: List<Value>, options: Map<String, Value>, rest: List<Value>): Value {
        val a = env.expectConvert<Int>(params[0])
        val b = env.expectConvert<Int>(params[1])
        return Value(min(a, b))
    }
}