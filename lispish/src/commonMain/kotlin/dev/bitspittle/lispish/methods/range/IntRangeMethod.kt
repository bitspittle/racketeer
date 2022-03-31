package dev.bitspittle.lispish.methods.range

import dev.bitspittle.lispish.Environment
import dev.bitspittle.lispish.Method
import dev.bitspittle.lispish.Value
import dev.bitspittle.lispish.converters.IntToIntRangeConverter
import dev.bitspittle.lispish.converters.PlaceholderConverter

class IntRangeMethod : Method("..", 2) {
    override fun invoke(env: Environment, params: List<Value>): Value {
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