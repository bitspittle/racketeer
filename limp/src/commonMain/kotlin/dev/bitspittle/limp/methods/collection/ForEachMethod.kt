package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr

/**
 * for-each (List) ('Expr)
 *
 * Take a list and run an expression on each item in the list.
 */
class ForEachMethod : Method("for-each", 2) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val list = env.expectConvert<List<Any>>(params[0])
        val transform = env.expectConvert<Expr>(params[1])

        return list.forEach { item ->
            env.scoped { // Don't let values defined during the lambda escape
                env.expectConvert<Any>(eval.extend(mapOf("\$it" to item)).evaluate(env, transform))
            }
        }
    }
}