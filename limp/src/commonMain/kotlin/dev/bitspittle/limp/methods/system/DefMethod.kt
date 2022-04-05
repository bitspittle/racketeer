package dev.bitspittle.limp.methods.system

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.types.Expr

/**
 * Define a method that lets you define a method...
 *
 * The method should take at least two arguments, a name and a method body, but optionally one or more arguments that,
 * if present, will be bound to as temporary variables.
 *
 * For example, if the `clamp` method wasn't already provided by the system:
 *
 * ```
 * def 'clamp '$val '$low '$hi '(min (max $val $low) $hi)
 * ```
 *
 * you can see that the first argument, `'clamp`, is the name, followed by three parameters, followed by the method
 * body `'(min ...)`.
 */
class DefMethod : Method("def", 0, consumeRest = true) {
    override suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        require(rest.size >= 2) { "The \"def\" method was not provided with enough arguments. It needs at least a name and a body." }
        val nameExpr = env.expectConvert<Expr>(rest[0])
        val bodyExpr = env.expectConvert<Expr>(rest.last())

        val nameIdentifier = nameExpr as? Expr.Identifier ?: throw EvaluationException(
            nameExpr.ctx,
            "First argument to \"def\" should be a simple identifier name, e.g. \"'\$example\"."
        )

        val argIds = rest.subList(1, rest.lastIndex).mapIndexed { i, value ->
            val argExpr = env.expectConvert<Expr>(value)
            argExpr as? Expr.Identifier ?: throw EvaluationException(
                argExpr.ctx,
                "Parameter #${i + 1} should be a simple identifier name, e.g. \"'\$example\"."
            )
        }

        env.addMethod(object : Method(nameIdentifier.name, argIds.size) {
            override suspend fun invoke(
                env: Environment,
                params: List<Any>,
                options: Map<String, Any>,
                rest: List<Any>
            ): Any {
                return env.scoped {
                    val evaluator = Evaluator(params.mapIndexed { i, value -> argIds[i].name to value }.toMap())
                    evaluator.evaluate(env, bodyExpr)
                }
            }
        })

        return Unit
    }
}