package dev.bitspittle.limp.methods.math

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.Value

class AddListMethod : Method("sum", 1) {
    override fun invoke(env: Environment, params: List<Value>, options: Map<String, Value>, rest: List<Value>): Value {
        val ints = env.expectConvert<ArrayList<Int>>(params[0])
        return Value(ints.sum())
    }
}