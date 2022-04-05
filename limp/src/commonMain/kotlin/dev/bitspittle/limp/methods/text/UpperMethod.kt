package dev.bitspittle.limp.methods.text

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method

class UpperMethod : Method("upper", 1) {
    override suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        return env.expectConvert<String>(params[0]).uppercase()
    }
}