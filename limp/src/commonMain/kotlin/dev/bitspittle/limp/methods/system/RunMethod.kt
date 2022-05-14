package dev.bitspittle.limp.methods.system

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.listTypeOf
import dev.bitspittle.limp.types.Expr

/**
 * run ('Expr) ('Expr) ('Expr)... -> Any
 *
 * Consume all remaining arguments, which should all be expressions, and run them in order. Return the value returned
 * from the last expression.
 *
 * This method is a useful way to run may expressions within an expression (e.g. a branch in an if/else condition), and
 * to ensure that some code runs from left to right (where things are by default evaluated right to left).
 *
 * Any variable or method defined within a run block will be dropped by the end of it.
 */
class RunMethod : Method("run", 0, consumeRest = true) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val exprs = env.expectConvert<List<Expr>>(rest, listTypeOf())

        var result: Any = Unit

        env.scoped {
            exprs.forEach { expr ->
                result = eval.evaluate(env, expr)
            }
        }

        return result
    }
}