package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.Value
import dev.bitspittle.limp.converters.PlaceholderConverter
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.types.ListStrategy

/**
 * Take a list and return the ONLY element in it that matches some test expression, or throw an error.
 *
 * In other words, only use this when you're sure there's only exactly one match.
 */
class SingleMethod : Method("single", 2) {
    override suspend fun invoke(env: Environment, params: List<Value>, options: Map<String, Value>, rest: List<Value>): Value {
        val list = env.expectConvert<List<Any>>(params[0])
        val predicate = env.expectConvert<Expr>(params[1])

        return Value(list.single { item ->
            env.expectConvert(Evaluator(mapOf("\$it" to Value(item))).evaluate(env, predicate))
        })
    }
}