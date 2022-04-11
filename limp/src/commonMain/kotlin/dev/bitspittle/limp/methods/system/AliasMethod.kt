package dev.bitspittle.limp.methods.system

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.converters.PlaceholderConverter
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.types.Expr

class AliasMethod : Method("alias", 2) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val aliasExpr = env.expectConvert<Expr>(params[0])
        val aliasIdentifier = aliasExpr as? Expr.Identifier ?: throw EvaluationException(aliasExpr.ctx, "First argument to \"alias\" should be a simple identifier name, e.g. \"'\$example\".")
        val forNameExpr = env.expectConvert<Expr>(params[1])
        val forNameIdentifier = forNameExpr as? Expr.Identifier ?: throw EvaluationException(forNameExpr.ctx, "Second argument to \"alias\" should be a simple identifier name, e.g. \"'\$example\".")

        val allowOverwrite = env.scoped {
            env.addConverter(PlaceholderConverter(true))
            options["overwrite"]?.let { env.expectConvert(it) }
        } ?: false

        env.addAlias(aliasIdentifier.name, forNameIdentifier.name, allowOverwrite)
        return Unit
    }
}