package dev.bitspittle.limp.methods.text

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.converters.CharToStringConverter

class LowerMethod : Method("lower", 1) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        return env.scoped {
            env.addConverter(CharToStringConverter())
            val str = env.expectConvert<String>(params[0])
            str.lowercase()
        }
    }
}