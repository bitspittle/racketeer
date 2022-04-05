package dev.bitspittle.limp.methods.logic

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.converters.PlaceholderConverter
import dev.bitspittle.limp.types.Expr

class IfMethod : Method("if", 3) {
    override suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val cond = env.expectConvert<Boolean>(params[0])

        return env.scoped {
            env.addConverter(PlaceholderConverter(Expr.Empty, Expr::class))

            val trueExpr = env.expectConvert<Expr>(params[1])
            val falseExpr = env.expectConvert<Expr>(params[2])

            val evaluator = Evaluator()
            evaluator.evaluate(env, if (cond) trueExpr else falseExpr)
        }
    }
}