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

/**
 * An expression represents a sequence that is parsable in the Limp language. Both the smallest primitives and the
 * whole line of code are both considered expressions.
 *
 * The following line demonstrates all expressions in this simple language:
 *
 * ```
 * union (take --from 'back $letters 3) (filter $numbers '(> $it 0)) (list "hello" "world")
 * ```
 *
 * - Text is a quoted string, e.g. "hello" and "world"
 * - Number is a numeric value, e.g. 3 and 0
 * - Identifier is all other text, e.g. "union", "$letters", ">"
 * - Option indicates that the next expression should be passed into a method indirectly, e.g. "--from 'back"
 * - Deferred is an expression prepended with a apostrophe, e.g. '(> $it 0)
 * - Chain is a list of two or more expressions, e.g. "take, $letters, 3"; also, "union, (...), (...), (...)"
 * - A block is parens wrapping an expression, e.g. (take $letters 3)
 *   Blocks can be nested.
 */
sealed class Expr(val ctx: ExprContext) {
    companion object {
        /** A dummy expression that does nothing when evaluated. */
        val Empty = Stub(Unit)

        fun parse(code: String): Expr {
            val ctx = ParserContext(code)
            val genericParseError = "Parsing failed after encountering an unexpected character."
            val result = ExprParser().tryParse(ctx) ?: throw ParseException(ctx, genericParseError)

            if (!result.ctx.isFinished) {
                throw ParseException(
                    result.ctx,
                    if (result.ctx.startsWith(")")) "Extra closed parentheses was found." else genericParseError
                )
            }

            return result.value
        }
    }

    class Text(val text: String, ctx: ExprContext) : Expr(ctx)
    class Number(val number: Int, ctx: ExprContext) : Expr(ctx)
    class Identifier(val name: String, ctx: ExprContext) : Expr(ctx)
    class Option(val identifier: Identifier, ctx: ExprContext) : Expr(ctx)
    class Deferred(val expr: Expr, ctx: ExprContext) : Expr(ctx)
    class Chain(val exprs: List<Expr>, ctx: ExprContext) : Expr(ctx)
    class Block(val expr: Expr, ctx: ExprContext) : Expr(ctx)

    /**
     * An expression not meant to be parsed but to be created manually if you want to stub out the expression passed
     * into a method that should evaluate to some value.
     */
    class Stub(val value: Any) : Expr(value.toString().let { valueStr -> ExprContext(valueStr, 0, valueStr.length) })

    override fun toString(): String {
        return ctx.text
    }
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
