package dev.bitspittle.limp

import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.types.walk

/**
 * @param transients A list of extra variables which only exist during the evaluation, without affecting the
 *   underlying environment. Transients take priority and will intercept a variable in the environment with the same
 *   name, in case of conflicts. Transients are a useful way to ensure that a handful of variables don't leak outside a
 *   very tight scope. For example, when defining methods, you want to bind parameters to local values that don't
 *   escape from that method. This could also be a good place to define variables that only exist within the
 *   lifetime of some lambda call, e.g. the `$it` value in `filter`.
 */
class Evaluator private constructor(private val transients: Map<String, Any>, private val closures: MutableMap<Expr, MutableList<Map<String, Any>>>) {
    constructor() : this(emptyMap(), mutableMapOf())

    /**
     * Extend this evaluator with more transients.
     *
     * This effectively supports closures. This layers a new evaluator on top of the existing one, where transients from
     * an underlying layer won't leak into this evaluator, EXCEPT for expressions that were already evaluated by the
     * parent evaluator.
     *
     * A concrete case can help here:
     *
     * ```
     * def 'filter-gte '$list '$low '(filter $list '(>= $it $low))
     * ```
     *
     * Above, the "filter" method itself, internally, should not have access to "$list" or "$low" values, but the
     * lambda we pass *into* filter should have access to them. The way the above works is that, when you call "def",
     * you have one evaluator, which defines the "$list" and "$low" variables, and then wraps all expressions with
     * closures that can access them (including the ">= $it $low" lambda). Then, filter internally "extends" the root
     * evaluator, which inherits its closures but NOT the variables "$list" and "$low". Phew.
     */
    fun extend(transients: Map<String, Any>) = Evaluator(transients, closures)

    suspend fun evaluate(env: Environment, code: String): Any {
        return evaluate(env, Expr.parse(code))
    }

    suspend fun evaluate(env: Environment, expr: Expr): Any {
        expr.walk().forEach {
            closures.getOrPut(it) { mutableListOf() }.add(transients)
        }
        try {
            return handleEvaluate(env, expr)
        }
        finally {
            expr.walk().forEach {
                closures.getValue(it).apply {
                    remove(transients)
                    if (this.isEmpty()) {
                        closures.remove(it)
                    }
                }
            }
        }
    }

    private suspend fun handleEvaluate(env: Environment, expr: Expr): Any {
        return when (expr) {
            is Expr.Text -> evaluateText(expr)
            is Expr.Number -> evaluateNumber(expr)
            is Expr.Identifier -> evaluateIdentifier(env, expr)
            is Expr.Option -> evaluateOption(expr)
            is Expr.Deferred -> evaluateDeferred(expr)
            is Expr.Chain -> evaluateChain(env, expr)
            is Expr.Block -> evaluateBlock(env, expr)
            is Expr.Stub -> expr.value
        }
    }

    private fun evaluateText(textExpr: Expr.Text): Any {
        return textExpr.text
    }
    private fun evaluateNumber(numberExpr: Expr.Number): Any {
        return numberExpr.number
    }
    private suspend fun evaluateIdentifier(
        env: Environment,
        identExpr: Expr.Identifier,
        values: MutableList<Any> = mutableListOf(),
        options: MutableMap<String, Any> = mutableMapOf()
    ): Any {
        return closures[identExpr]?.reversed()?.firstNotNullOfOrNull { transients -> transients[identExpr.name] }
            ?: env.loadValue(identExpr.name)
            ?: env.getMethod(identExpr.name)?.let { method ->
                if (method.numArgs > values.size) {
                    throw EvaluationException(identExpr.ctx, "Method \"${identExpr.name}\" takes ${method.numArgs} argument(s) but only ${values.size} was/were provided.")
                }
                val params = values.subList(0, method.numArgs)
                val trackedOptions = TrackedMap(options)
                val rest = if (method.consumeRest) values.subList(method.numArgs, values.size - method.numArgs) else mutableListOf()
                try {
                    val result = method.invoke(env, this, params, trackedOptions, rest)
                    if (method.consumeRest) values.clear() else params.clear()
                    options.keys.removeAll(trackedOptions.accessedKeys)
                    if (options.isNotEmpty()) {
                        throw EvaluationException(identExpr.ctx, "Method \"${identExpr.name}\" was handed optional parameter(s) it did not use: ${options.keys}.")
                    }

                    result
                }
                catch (ex: EvaluationException) {
                    // If our method was kind enough to already throw an evaluation exception for us, use it, as it
                    // probably has more relevant context for where the issue actually happened.
                    throw ex
                }
                catch (ex: Exception) {
                    throw EvaluationException(identExpr.ctx, "Method \"${identExpr.name}\" threw an exception while trying to run:\n> ${ex.message}", cause = ex)
                }
            }
            ?: throw EvaluationException(identExpr.ctx, "Could not resolve identifier \"${identExpr.name}\" as either a variable or a method.")
    }

    private fun evaluateOption(
        optionExpr: Expr.Option,
        values: MutableList<Any> = mutableListOf(),
        options: MutableMap<String, Any> = mutableMapOf()
    ): Any {
        if (values.isEmpty()) {
            throw EvaluationException(optionExpr.ctx, "Optional parameter specifier \"${optionExpr.identifier.name}\" was not followed by a value.")
        }

        options[optionExpr.identifier.name] = values.removeFirst()
        return Unit
    }

    private fun evaluateDeferred(deferredExpr: Expr.Deferred): Any {
        return deferredExpr.expr
    }
    private suspend fun evaluateChain(env: Environment, chainExpr: Expr.Chain): Any {
        // Work backwards, so we can call methods with arguments we resolved earlier...
        // e.g. "+ 1 * 2 3" becomes ...
        // - 3
        // - 3, 2
        // - 3, 2, * -> 6
        // - 6, 1
        // - 6, 1, + -> 7
        val evaluated = mutableListOf<Any>()
        val options = mutableMapOf<String, Any>()
        chainExpr.exprs.reversed().forEach { expr ->
            val value = when(expr) {
                is Expr.Identifier -> evaluateIdentifier(env, expr, evaluated, options)
                // Options consume values and return EMPTY. Don't return the useless result.
                is Expr.Option -> { evaluateOption(expr, evaluated, options); null }
                else -> handleEvaluate(env, expr)
            }
            value?.let { evaluated.add(0, it) }
        }

        if (evaluated.size != 1) {
            throw EvaluationException(chainExpr.ctx, "An expression did not consume all its arguments. Did you add an extra, unnecessary value somewhere?")
        }

        return evaluated[0]
    }

    private suspend fun evaluateBlock(env: Environment, blockExpr: Expr.Block): Any {
        return handleEvaluate(env, blockExpr.expr)
    }
}