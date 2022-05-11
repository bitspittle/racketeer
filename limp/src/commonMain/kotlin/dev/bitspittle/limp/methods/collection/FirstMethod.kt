package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr

/**
 * first --matching ('Expr) (List) -> Any
 *
 * Take a list and an optional test expression and return the first element in the list that matches the test
 * expression, or throw an error. (If no test expression is provided, then match all items).
 *
 * In other words, only use this when you're sure there's at least one match.
 */
class FirstMethod : Method("first", 1) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val list = env.expectConvert<List<Any>>(params[0])
        val predicate = options["matching"]?.let { matching ->
            env.expectConvert<Expr>(matching)
        }

        return if (predicate != null) {
            list.first { item ->
                env.scoped { // Don't let values defined during the lambda escape
                    env.expectConvert(eval.extend(mapOf("\$it" to item)).evaluate(env, predicate))
                }
            }
        } else {
            list.first()
        }
    }
}