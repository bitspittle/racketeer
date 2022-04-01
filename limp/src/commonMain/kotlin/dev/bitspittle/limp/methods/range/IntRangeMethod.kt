package dev.bitspittle.limp.methods.range

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.Value
import dev.bitspittle.limp.converters.PlaceholderConverter

class IntRangeMethod : Method("..", 2) {
    override fun invoke(env: Environment, params: List<Value>, optionals: Map<String, Value>, rest: List<Value>): Value {
        val start = env.scoped {
            env.add(PlaceholderConverter(0))
            env.expectConvert<Int>(params[0])
        }

        val end = env.scoped {
            env.add(PlaceholderConverter(Int.MAX_VALUE))
            env.expectConvert<Int>(params[1])
        }

        return Value(IntRange(start, end))
    }
}