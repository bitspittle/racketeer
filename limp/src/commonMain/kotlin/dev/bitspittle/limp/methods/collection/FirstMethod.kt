package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.converters.PlaceholderConverter
import dev.bitspittle.limp.types.Expr

/**
 * Take a list and return the first element in it that matches some test expression, or throw an error.
 *
 * In other words, only use this when you're sure there's at least one match.
 */
class FirstMethod : Method("first", 2) {
    override suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val list = env.expectConvert<List<Any>>(params[0])
        val predicate = env.scoped {
            env.addConverter(PlaceholderConverter(Expr.Stub(true), toClass = Expr::class))
            env.expectConvert<Expr>(params[1])
        }

        return list.first { item ->
            env.expectConvert(Evaluator(mapOf("\$it" to item)).evaluate(env, predicate))
        }
    }
}