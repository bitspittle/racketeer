package dev.bitspittle.limp.parser.parsers.text

import dev.bitspittle.limp.parser.ParseResult
import dev.bitspittle.limp.parser.Parser
import dev.bitspittle.limp.parser.ParserContext
import dev.bitspittle.limp.parser.parsers.combinators.map
import dev.bitspittle.limp.utils.ifTrue

class AnyCharParser : Parser<Char> {
    override fun tryParse(ctx: ParserContext): ParseResult<Char>? {
        return ctx.getChar()?.let { c -> ParseResult(ctx.incStart(), c) }
    }
}

class LetterParser : Parser<Char> {
    override fun tryParse(ctx: ParserContext): ParseResult<Char>? {
        return ctx.getChar()?.takeIf { c -> c.isLetter() }?.let { c -> ParseResult(ctx.incStart(), c) }
    }
}

class WhitespaceParser : Parser<Char> {
    override fun tryParse(ctx: ParserContext): ParseResult<Char>? {
        return ctx.getChar()?.takeIf { c -> c.isWhitespace() }?.let { c -> ParseResult(ctx.incStart(), c) }
    }
}

class DigitParser : Parser<Char> {
    override fun tryParse(ctx: ParserContext): ParseResult<Char>? {
        return ctx.getChar()?.takeIf { c -> c.isDigit() }?.let { c -> ParseResult(ctx.incStart(), c) }
    }
}

class LetterOrDigitParser : Parser<Char> {
    override fun tryParse(ctx: ParserContext): ParseResult<Char>? {
        return ctx.getChar()?.takeIf { c -> c.isLetterOrDigit() }?.let { c -> ParseResult(ctx.incStart(), c) }
    }
}

class NonWhitespaceParser : Parser<Char> {
    override fun tryParse(ctx: ParserContext): ParseResult<Char>? {
        return ctx.getChar()?.takeIf { c -> !c.isWhitespace() }?.let { c -> ParseResult(ctx.incStart(), c) }
    }
}

class EndOfTextParser : Parser<Unit> {
    override fun tryParse(ctx: ParserContext): ParseResult<Unit>? {
        return ctx.isFinished.ifTrue { ParseResult(ctx, Unit) }
    }
}

class TextParser : Parser<String> {
    override fun tryParse(ctx: ParserContext): ParseResult<String>? {
        val str = ctx.takeWhile { c -> !c.isWhitespace() }
        return str.isNotEmpty().ifTrue { ParseResult(ctx.incStart(str.length), str) }
    }
}

class WordParser : Parser<String> {
    override fun tryParse(ctx: ParserContext): ParseResult<String>? {
        val str = ctx.takeWhile { c -> c.isLetter() }
        return str.isNotEmpty().ifTrue { ParseResult(ctx.incStart(str.length), str) }
    }
}

class IntParser : Parser<Int> {
    @Suppress("NAME_SHADOWING")
    override fun tryParse(ctx: ParserContext): ParseResult<Int>? {
        var ctx = ctx
        val isNegative = EatCharParser('-').tryParse(ctx)?.let { result ->
            ctx = result.ctx
            true
        } ?: false
        val str = ctx.takeWhile { c -> c.isDigit() }
        return str.isNotEmpty().ifTrue { ParseResult(ctx.incStart(str.length), str.toInt() * if (isNegative) -1 else 1) }
    }
}

class MatchCharParser(private val c: Char) : Parser<Char> {
    override fun tryParse(ctx: ParserContext): ParseResult<Char>? {
        return (ctx.getChar() == c).ifTrue { ParseResult(ctx.incStart(), c) }
    }
}

class MatchTextParser(private val text: String) : Parser<String> {
    override fun tryParse(ctx: ParserContext): ParseResult<String>? {
        return ctx.startsWith(text).ifTrue { ParseResult(ctx.incStart(text.length), text) }
    }
}

class EatCharParser(private val c: Char) : Parser<Unit> {
    override fun tryParse(ctx: ParserContext): ParseResult<Unit>? {
        return MatchCharParser(c).map { }.tryParse(ctx)  // Convert to Unit
    }
}

class EatAnyWhitespaceParser : Parser<Unit> {
    override fun tryParse(ctx: ParserContext): ParseResult<Unit> {
        val str = ctx.takeWhile { c -> c.isWhitespace() }
        return ParseResult(ctx.incStart(str.length), Unit)
    }
}

class EatTextParser(private val text: String) : Parser<Unit> {
    override fun tryParse(ctx: ParserContext): ParseResult<Unit>? {
        return MatchTextParser(text).map { }.tryParse(ctx)  // Convert to Unit
    }
}

class EatAnyWhileParser(private val cond: (Char) -> Boolean) : Parser<Unit> {
    override fun tryParse(ctx: ParserContext): ParseResult<Unit> {
        val str = ctx.takeWhile(cond)
        return ParseResult(ctx.incStart(str.length), Unit)
    }
}
