package dev.bitspittle.limp.methods.math

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.Value
import kotlin.math.max
import kotlin.math.min

class ClampMethod : Method("clamp", 3) {
    override fun invoke(env: Environment, params: List<Value>, options: Map<String, Value>, rest: List<Value>): Value {
        val value = env.expectConvert<Int>(params[0])
        val low = env.expectConvert<Int>(params[1])
        val hi = env.expectConvert<Int>(params[2])
        return Value(value.coerceIn(low, hi))
    }
}