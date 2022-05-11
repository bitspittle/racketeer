package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.converters.ValueToExprConverter
import dev.bitspittle.limp.types.Expr

/**
 * in? (List) ('Expr | Any) -> Boolean
 *
 * Returns whether a passed in expression or item matches any in the list.
 */
class InMethod : Method("in?", 2) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        return (IndexOfMethod().invoke(env, eval, params, options, rest) as Int) >= 0
    }
}