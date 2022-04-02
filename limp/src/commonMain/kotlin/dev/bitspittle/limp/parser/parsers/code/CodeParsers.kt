package dev.bitspittle.limp.parser.parsers.code

import dev.bitspittle.limp.exceptions.ParseException
import dev.bitspittle.limp.parser.ParseResult
import dev.bitspittle.limp.parser.Parser
import dev.bitspittle.limp.parser.ParserContext
import dev.bitspittle.limp.parser.parsers.combinators.*
import dev.bitspittle.limp.parser.parsers.text.*
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.types.ExprContext
import dev.bitspittle.limp.types.from

private val ALLOWED_SYMBOLS = listOf('$', '_', '%', '+', '-', '*', '^', '/', '|', '&', '!', '?', '.', '=', '<', '>')
private val END_BOUNDARY_CHARS = listOf(')', '#')

/** Parsing for everything that evaluates to a single item in an expression chain.
 *
 * Basically, everything but the expression chain itself. This approach allows us to avoid
 * an issue with infinite recursion.
 */
class SingleExprParser : Parser<Expr> {
    override fun tryParse(ctx: ParserContext): ParseResult<Expr>? {
        NumberExprParser().tryParse(ctx)?.let { return it }
        TextExprParser().tryParse(ctx)?.let { return it }
        DeferredExprParser().tryParse(ctx)?.let { return it }
        OptionExprParser().tryParse(ctx)?.let { return it }
        IdentifierExprParser().tryParse(ctx)?.let { return it }
        BlockExprParser().tryParse(ctx)?.let { return it }

        return null
    }
}

class ExprParser : Parser<Expr> {
    override fun tryParse(ctx: ParserContext): ParseResult<Expr>? {
        val result = ChainExprParser().tryParse(ctx) ?: return null
        return if (result.value.exprs.size == 1) result.map { it.exprs[0] } else { result }
    }
}

class NumberExprParser : Parser<Expr.Number> {
    override fun tryParse(ctx: ParserContext): ParseResult<Expr.Number>? {
        val result = IntParser().tryParse(ctx) ?: return null
        return result.map { Expr.Number(result.value, ExprContext.from(ctx, result)) }
    }
}

class TextExprParser : Parser<Expr.Text> {
    override fun tryParse(ctx: ParserContext): ParseResult<Expr.Text>? {
        val result = (MatchCharParser('"') then
                (MatchTextParser("\\\"").map { '"' } or
                        AnyCharParser().withPredicate { c -> c != '"' })
                    .zeroOrMore() then
                MatchCharParser('"')
                )
            .tryParse(ctx) ?: return null

        // The above creates a nested structure like:
        // ((" to text) to ")
        // The text we want is the second part of the first item
        val text = result.value.first.value.second.value.joinToString("")

        return result.map { Expr.Text(text, ExprContext.from(ctx, result)) }
    }
}

class EatCommentParser : Parser<Unit> {
    override fun tryParse(ctx: ParserContext): ParseResult<Unit>? {
        return EatIfMatchAllParser(
            MatchCharParser('#'),
            EatAnyWhileParser { c -> c != '\n' } then EatCharParser('\n').optional()
        ).tryParse(ctx)
    }
}

class IdentifierExprParser : Parser<Expr.Identifier> {
    private fun isAllowedChar(c: Char): Boolean = c.isLetterOrDigit() || c in ALLOWED_SYMBOLS

    override fun tryParse(ctx: ParserContext): ParseResult<Expr.Identifier>? {
        val firstLetterParser = AnyCharParser().withPredicate(::isAllowedChar)
        // If the first letter matches, we should consume the rest of the identifier greedily. If it ends up containing
        // invalid characters, instead of stopping at that point, just eat them up now and throw an error message
        // below.
        val remainingLettersParser = NonWhitespaceParser().withPredicate { c -> c !in END_BOUNDARY_CHARS }.zeroOrMore()

        val result = (firstLetterParser then remainingLettersParser).tryParse(ctx) ?: return null
        val identifier = result.first.value + result.second.value.joinToString("")
        if (!identifier.all(::isAllowedChar)) {
            throw ParseException(
                ctx,
                identifier.length,
                "Invalid identifier \"$identifier\" contains invalid characters.\n\nAllowed: letters, numbers, and $ALLOWED_SYMBOLS."
            )
        }

        return result.map { Expr.Identifier(identifier, ExprContext.from(ctx, result)) }
    }
}

class DeferredExprParser : Parser<Expr.Deferred> {
    override fun tryParse(ctx: ParserContext): ParseResult<Expr.Deferred>? {
        val result = (MatchCharParser('\'') then SingleExprParser()).tryParse(ctx) ?: return null
        return result.map { Expr.Deferred(result.second.value, ExprContext.from(ctx, result)) }
    }
}

class OptionExprParser : Parser<Expr.Option> {
    override fun tryParse(ctx: ParserContext): ParseResult<Expr.Option>? {
        val result = (MatchTextParser("--") then IdentifierExprParser()).tryParse(ctx) ?: return null
        return result.map { Expr.Option(result.second.value, ExprContext.from(ctx, result)) }
    }
}

class ChainExprParser : Parser<Expr.Chain> {
    override fun tryParse(ctx: ParserContext): ParseResult<Expr.Chain>? {
        val result =
            (EatAnyWhitespaceParser() then SingleExprParser() then EatAnyWhitespaceParser() then EatCommentParser().optional()).oneOrMore()
                .tryParse(ctx)
                ?: return null

        // The above parsing creates a List<(((Whitespace to Expr) to Whitespace) to Comment)> chain. We only care
        // about the inner "Expr" part. It's gnarly to peel it out, but that's what we're doing below!
        val exprs = result.value.map { it.first.value.first.value.second.value }
        return result.map { Expr.Chain(exprs, ExprContext.from(ctx, result)) }
    }
}

class BlockExprParser : Parser<Expr.Block> {
    override fun tryParse(ctx: ParserContext): ParseResult<Expr.Block>? {
        val openParenResult = MatchCharParser('(').tryParse(ctx) ?: return null
        val innerExpr = ExprParser().tryParse(openParenResult.ctx) ?: throw ParseException(
            ctx,
            "Malformed or missing expression."
        )
        val result = MatchCharParser(')').tryParse(innerExpr.ctx) ?: throw ParseException(
            ctx,
            "Open parentheses is missing a matching closing parentheses."
        )

        return result.map { Expr.Block(innerExpr.value, ExprContext.from(ctx, result)) }
    }
}
