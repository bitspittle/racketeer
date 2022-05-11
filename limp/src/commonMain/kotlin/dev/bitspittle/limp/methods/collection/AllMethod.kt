package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.converters.ValueToExprConverter
import dev.bitspittle.limp.types.Expr

/**
 * all? (List) ('Expr) -> Boolean
 *
 * Returns true if the expression returns true for every item in the list.
 */
class AllMethod : Method("all?", 2) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val list = env.expectConvert<List<Any>>(params[0])
        val predicate = env.expectConvert<Expr>(params[1])
        return list.all { item ->
            env.scoped { // Don't let values defined during the lambda escape
                env.expectConvert(eval.extend(mapOf("\$it" to item)).evaluate(env, predicate))
            }
        }
    }
}
