package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.Value
import dev.bitspittle.limp.converters.PlaceholderConverter
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.types.ListStrategy

/**
 * Take a list and an expression and return a new list with that expression applied on each element.
 */
class MapMethod : Method("map", 2) {
    override fun invoke(env: Environment, params: List<Value>, options: Map<String, Value>, rest: List<Value>): Value {
        val list = env.expectConvert<List<Any>>(params[0])
        val transform = env.expectConvert<Expr>(params[1])

        val evaluator = Evaluator()
        return Value(list.map { item ->
            env.scoped {
                env.storeValue("\$it", Value(item))
                env.expectConvert<Any>(evaluator.evaluate(env, transform))
            }
        })
    }
}