package dev.bitspittle.limp.methods.system

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.converters.PlaceholderConverter
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.types.Logger

/**
 * Store a value with some variable name label.
 */
class SetMethod(private val logger: Logger) : Method("set", 2) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val nameExpr = env.expectConvert<Expr>(params[0])
        val nameIdentifier = nameExpr as? Expr.Identifier ?: throw EvaluationException(nameExpr.ctx, "First argument to \"set\" should be a simple identifier name, e.g. \"'\$example\".")

        val allowOverwrite = env.scoped {
            env.addConverter(PlaceholderConverter(true))
            options["overwrite"]?.let { env.expectConvert(it) }
        } ?: false

        if (!nameIdentifier.name.startsWith("$")) {
            logger.warn("Variable names are expected to start with a '$', but got \"${nameIdentifier.name}\".")
        }

        env.storeValue(nameIdentifier.name, params[1], allowOverwrite)

        return Unit
    }
}