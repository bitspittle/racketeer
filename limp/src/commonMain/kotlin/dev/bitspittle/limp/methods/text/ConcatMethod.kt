package dev.bitspittle.limp.methods.text

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.Value
import dev.bitspittle.limp.converters.CharToStringConverter
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.types.Expr

class ConcatMethod : Method("concat", 2) {
    override suspend fun invoke(env: Environment, params: List<Value>, options: Map<String, Value>, rest: List<Value>): Value {
        return env.scoped {
            env.addConverter(CharToStringConverter())
            val str1 = env.expectConvert<String>(params[0])
            val str2 = env.expectConvert<String>(params[1])
            Value(str1 + str2)
        }
    }
}