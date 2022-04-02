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
            is Expr.Option -> evaluateOption(expr)
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
    private fun evaluateIdentifier(
        env: Environment,
        identExpr: Expr.Identifier,
        values: MutableList<Value> = mutableListOf(),
        options: MutableMap<String, Value> = mutableMapOf()
    ): Value {
        return env.getValue(identExpr.name)
            ?: env.getMethod(identExpr.name)?.let { method ->
                if (method.numArgs > values.size) {
                    throw EvaluationException(identExpr.ctx, "Method \"${identExpr.name}\" takes ${method.numArgs} argument(s) but only ${values.size} was/were provided.")
                }
                val params = values.subList(0, method.numArgs)
                val options = SelfDestructingMap(options)
                val rest = if (method.consumeRest) values.subList(method.numArgs, values.size - method.numArgs) else mutableListOf()
                try {
                    method.invoke(env, params, options, rest)
                }
                catch (ex: Exception) {
                    throw EvaluationException(identExpr.ctx, "Method \"${identExpr.name}\" threw an exception while trying to run:\n> ${ex.message}")
                }
                finally {
                    if (method.consumeRest) values.clear() else params.clear()
                    if (options.isNotEmpty()) {
                        throw EvaluationException(identExpr.ctx, "Method \"${identExpr.name}\" was handed optional parameter(s) it did not consume: ${options.keys}.")
                    }
                } // TODO: Support rest
            }
            ?: throw EvaluationException(identExpr.ctx, "Could not resolve identifier \"${identExpr.name}\" as either a variable or a method.")
    }

    private fun evaluateOption(
        optionExpr: Expr.Option,
        values: MutableList<Value> = mutableListOf(),
        options: MutableMap<String, Value> = mutableMapOf()
    ): Value {
        if (values.isEmpty()) {
            throw EvaluationException(optionExpr.ctx, "Optional parameter specifier \"${optionExpr.identifier.name}\" was not followed by a value.")
        }

        options[optionExpr.identifier.name] = values.removeFirst()
        return Value.Empty
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
        val options = mutableMapOf<String, Value>()
        chainExpr.exprs.reversed().forEach { expr ->
            val value = when(expr) {
                is Expr.Identifier -> evaluateIdentifier(env, expr, evaluated, options)
                // Options consume values and return EMPTY. Don't return the useless result.
                is Expr.Option -> { evaluateOption(expr, evaluated, options); null }
                else -> evaluate(env, expr)
            }
            value?.let { evaluated.add(0, it) }
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