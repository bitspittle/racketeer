package dev.bitspittle.limp.methods.range

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.converters.PlaceholderConverter

class IntRangeMethod : Method("..", 2) {
    override suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val step = options["step"]?.let { env.expectConvert<Int>(it) }

        val start = env.scoped {
            env.addConverter(PlaceholderConverter(0))
            env.expectConvert<Int>(params[0])
        }

        val end = env.scoped {
            env.addConverter(PlaceholderConverter(Int.MAX_VALUE))
            env.expectConvert<Int>(params[1])
        }

        return if (step != null) start .. end step step else IntRange(start, end)
    }
}