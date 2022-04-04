package dev.bitspittle.limp.methods.logic

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.Value
import kotlin.math.max
import kotlin.math.min

class AndMethod : Method("&&", 2) {
    override suspend fun invoke(env: Environment, params: List<Value>, options: Map<String, Value>, rest: List<Value>): Value {
        val a = env.expectConvert<Boolean>(params[0])
        val b = env.expectConvert<Boolean>(params[1])
        return Value(a && b)
    }
}