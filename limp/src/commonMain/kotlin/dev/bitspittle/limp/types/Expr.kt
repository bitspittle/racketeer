package dev.bitspittle.limp.types

import dev.bitspittle.limp.parser.ParseResult
import dev.bitspittle.limp.parser.ParserContext

class ExprContext(val code: String, val start: Int, val length: Int) {
    override fun toString(): String {
        return code.substring(start, start + length)
    }

    companion object {
        fun from(ctxStart: ParserContext, length: Int): ExprContext {
            require(length >= 0) { "Invalid length passed into ExprContext.from" }
            return ExprContext(ctxStart.text, ctxStart.startIndex, ctxStart.startIndex + length)
        }
    }
}

/**
 * Convenience method for simplifying [from] call.
 */
fun ExprContext.Companion.from(ctx: ParserContext, result: ParseResult<*>): ExprContext {
    require(ctx.text == result.ctx.text) { "Unrelated contexts passed in" }
    return ExprContext.from(ctx, result.ctx.startIndex - ctx.startIndex)
}

sealed class Expr(val ctx: ExprContext) {
    class Text(val text: String, ctx: ExprContext) : Expr(ctx)
    class Number(val number: Int, ctx: ExprContext) : Expr(ctx)
    class Identifier(val identifier: String, ctx: ExprContext) : Expr(ctx)
    class Deferred(val expr: Expr, ctx: ExprContext) : Expr(ctx)
    /** A list of expressions */
    class Chain(val exprs: List<Expr>, ctx: ExprContext) : Expr(ctx)
    /** A chain of expressions wrapped by parentheses. */
    class Block(val chain: Chain, ctx: ExprContext) : Expr(ctx)
}