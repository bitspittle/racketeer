package dev.bitspittle.limp.methods.system

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.Value
import dev.bitspittle.limp.types.Expr

/**
 * Store a value with some variable name label.
 */
class SetMethod : Method("set", 2) {
    override fun invoke(env: Environment, params: List<Value>, options: Map<String, Value>, rest: List<Value>): Value {
        val nameExpr = env.expectConvert<Expr>(params[0])
        val nameIdentifier = nameExpr as? Expr.Identifier ?: error("First argument to \"set\" should be a simple identifier name, e.g. \"'\$example\". Got: \"'${nameExpr.ctx.text}\".")

        env.storeValue(nameIdentifier.name, params[1])

        return Value.Empty
    }
}