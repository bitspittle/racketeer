package dev.bitspittle.limp.methods.text

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.Value

class UpperMethod : Method("upper", 1) {
    override suspend fun invoke(env: Environment, params: List<Value>, options: Map<String, Value>, rest: List<Value>): Value {
        return Value(env.expectConvert<String>(params[0]).uppercase())
    }
}