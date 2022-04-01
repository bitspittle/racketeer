package dev.bitspittle.limp.types

import dev.bitspittle.limp.exceptions.ParseException
import dev.bitspittle.limp.parser.ParseResult
import dev.bitspittle.limp.parser.ParserContext
import dev.bitspittle.limp.parser.parsers.code.ExprParser

class ExprContext(val code: String, val start: Int, val length: Int) {
    init {
        require(length >= 0) { "Negative length not allowed" }
        require(start + length <= code.length) { "Invalid code bounds" }
    }

    val text: String get() = code.substring(start, start + length)

    companion object {
        fun from(ctxStart: ParserContext, length: Int): ExprContext {
            return ExprContext(ctxStart.text, ctxStart.startIndex, length)
        }
    }
}

/**
 * Convenience method for simplifying [from] call.
 */
fun ExprContext.Companion.from(ctx: ParserContext, result: ParseResult<*>): ExprContext {
    require(ctx.text == result.ctx.text) { "Unrelated contexts passed in" }
    return from(ctx, result.ctx.startIndex - ctx.startIndex)
}

sealed class Expr(val ctx: ExprContext) {
    companion object {
        fun parse(code: String): Expr {
            val ctx = ParserContext(code)
            val result = ExprParser().tryParse(ctx) ?: throw ParseException(
                ctx,
                "Unknown parsing error. Please file a bug with the above code."
            )

            if (!result.ctx.isFinished) {
                throw ParseException(
                    result.ctx,
                    "Leftover code could not be parsed. Did you forget to close a right parentheses?"
                )
            }

            return result.value
        }
    }

    class Text(val text: String, ctx: ExprContext) : Expr(ctx)
    class Number(val number: Int, ctx: ExprContext) : Expr(ctx)
    class Identifier(val name: String, ctx: ExprContext) : Expr(ctx)
    class Deferred(val expr: Expr, ctx: ExprContext) : Expr(ctx)
    /** A list of expressions */
    class Chain(val exprs: List<Expr>, ctx: ExprContext) : Expr(ctx)
    /** A chain of expressions wrapped by parentheses. */
    class Block(val expr: Expr, ctx: ExprContext) : Expr(ctx)
}

fun Expr.walk(): Sequence<Expr> {
    val toVisit = mutableListOf(this)
    return sequence {
        while (toVisit.isNotEmpty()) {
            val currExpr = toVisit.removeFirst()
            yield(currExpr)
            when(currExpr) {
                is Expr.Deferred -> toVisit.add(0, currExpr.expr)
                is Expr.Chain -> toVisit.addAll(0, currExpr.exprs)
                is Expr.Block -> toVisit.add(0, currExpr.expr)
                else -> {}
            }
        }
    }
}
