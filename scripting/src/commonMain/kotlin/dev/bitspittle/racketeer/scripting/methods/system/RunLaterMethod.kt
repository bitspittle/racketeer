package dev.bitspittle.racketeer.scripting.methods.system

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.listTypeOf
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.racketeer.model.action.ActionQueue

/**
 * run-later ('Expr)
 *
 * Run an expression later instead of now, after all current actions are finished executing. This adds the expression
 * to the end of all actions.
 *
 * Note that a snapshot of the current environment is taken within the run-later expression, so that any variables
 * currently set are not lost.
 */
class RunLaterMethod(private val actionQueue: ActionQueue) : Method("run-later", 1) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val expr = env.expectConvert<Expr>(params[0])

        // Make a copy of the current environment, because otherwise all variables currently set would be long gone when
        // the deferred action runs.
        val envCopy = env.copy()
        actionQueue.enqueue {
            // Note: No need to scope envCopy to protect against set variables, because it's going to expire alone after
            // this method is finished anyway.
            eval.evaluate(envCopy, expr)
        }

        return Unit
    }
}