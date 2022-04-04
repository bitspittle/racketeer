package dev.bitspittle.limp.methods.math

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.Value

class MulListMethod : Method("mul", 1) {
    override suspend fun invoke(env: Environment, params: List<Value>, options: Map<String, Value>, rest: List<Value>): Value {
        val ints = env.expectConvert<List<Int>>(params[0])
        return Value(ints.fold(1) { acc, i -> acc * i })
    }
}