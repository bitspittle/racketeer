package dev.bitspittle.limp

import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.types.Expr

class Evaluator {
    fun evaluate(env: Environment, code: String): Value {
        return evaluate(env, Expr.parse(code))
    }

    private fun evaluate(env: Environment, expr: Expr): Value {
        return when (expr) {
            is Expr.Text -> evaluateText(expr)
            is Expr.Number -> evaluateNumber(expr)
            is Expr.Identifier -> evaluateIdentifier(env, expr)
            is Expr.Deferred -> evaluateDeferred(expr)
            is Expr.Chain -> evaluateChain(env, expr)
            is Expr.Block -> evaluateBlock(env, expr)
        }
    }

    private fun evaluateText(textExpr: Expr.Text): Value {
        return Value(textExpr.text)
    }
    private fun evaluateNumber(numberExpr: Expr.Number): Value {
        return Value(numberExpr.number)
    }
    private fun evaluateIdentifier(env: Environment, identExpr: Expr.Identifier, values: MutableList<Value> = mutableListOf()): Value {
        return env.getValue(identExpr.name)
            ?: env.getMethod(identExpr.name)?.let { method ->
                if (method.numArgs > values.size) {
                    throw EvaluationException(identExpr.ctx, "Method \"${identExpr.name}\" takes ${method.numArgs} argument(s) but only ${values.size} were provided.")
                }
                method.invoke(env, values).also {
                    values.subList(0, method.numArgs).clear()
                } // TODO: Support rest AND optional arguments
            }
            ?: throw EvaluationException(identExpr.ctx, "Could not resolve identifier \"${identExpr.name} as either a variable or a method.")
    }


    private fun evaluateDeferred(deferredExpr: Expr.Deferred): Value {
        return Value(deferredExpr.expr)
    }
    private fun evaluateChain(env: Environment, chainExpr: Expr.Chain): Value {
        // Work backwards, so we can call methods with arguments we resolved earlier...
        // e.g. "+ 1 * 2 3" becomes ...
        // - 3
        // - 3, 2
        // - 3, 2, * -> 6
        // - 6, 1
        // - 6, 1, + -> 7
        val evaluated = mutableListOf<Value>()
        chainExpr.exprs.reversed().forEach { expr ->
            evaluated.add(0, when(expr) {
                is Expr.Identifier -> evaluateIdentifier(env, expr, evaluated)
                else -> evaluate(env, expr)
            })
        }

        if (evaluated.size != 1) {
            throw EvaluationException(chainExpr.ctx, "An expression did not consume all its arguments. Did you add an extra, unnecessary value somewhere?")
        }

        return evaluated[0]
    }

    private fun evaluateBlock(env: Environment, blockExpr: Expr.Block): Value {
        return evaluate(env, blockExpr.expr)
    }
}