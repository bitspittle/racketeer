package dev.bitspittle.limp.methods.system

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.Value
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.types.Expr

/**
 * Store a value with some variable name label.
 */
class SetMethod : Method("set", 2) {
    override suspend fun invoke(env: Environment, params: List<Value>, options: Map<String, Value>, rest: List<Value>): Value {
        val nameExpr = env.expectConvert<Expr>(params[0])
        val nameIdentifier = nameExpr as? Expr.Identifier ?: throw EvaluationException(nameExpr.ctx, "First argument to \"set\" should be a simple identifier name, e.g. \"'\$example\".")

        env.storeValue(nameIdentifier.name, params[1])

        return Value.Empty
    }
}