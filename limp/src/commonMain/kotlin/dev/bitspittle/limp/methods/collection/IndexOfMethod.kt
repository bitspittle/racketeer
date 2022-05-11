package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.converters.ValueToExprConverter
import dev.bitspittle.limp.types.Expr

/**
 * index-of (List) ('Expr | Any) -> Int
 *
 * Take a list and either an expression OR a value, and return the first index that matches the expression / matches the
 * item.
 */
class IndexOfMethod : Method("index-of", 2) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val list = env.expectConvert<List<Any>>(params[0])

        val predicate = env.tryConvert<Expr>(params[1])
        return if (predicate != null) {
            list.indexOfFirst { item ->
                env.scoped { // Don't let values defined during the lambda escape
                    env.expectConvert(eval.extend(mapOf("\$it" to item)).evaluate(env, predicate))
                }
            }
        } else {
            list.indexOf(params[1])
        }
    }
}