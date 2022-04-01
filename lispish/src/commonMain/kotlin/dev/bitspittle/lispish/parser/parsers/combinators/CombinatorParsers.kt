package dev.bitspittle.lispish.parser.parsers.combinators

import dev.bitspittle.lispish.parser.ParseResult
import dev.bitspittle.lispish.parser.Parser
import dev.bitspittle.lispish.parser.ParserContext
import dev.bitspittle.lispish.utils.ifTrue

class PairParser<T1 : Any, T2 : Any>(private val parser1: Parser<T1>, private val parser2: Parser<T2>) :
    Parser<Pair<ParseResult<T1>, ParseResult<T2>>> {
    override fun tryParse(ctx: ParserContext): ParseResult<Pair<ParseResult<T1>, ParseResult<T2>>>? {
        val result1 = parser1.tryParse(ctx) ?: return null
        val result2 = parser2.tryParse(result1.ctx) ?: return null
        return ParseResult(result2.ctx, result1 to result2)
    }
}

class EitherParser<T: Any>(private val parser1: Parser<T>, private val parser2: Parser<T>) : Parser<T> {
    override fun tryParse(ctx: ParserContext): ParseResult<T>? {
        return parser1.tryParse(ctx) ?: parser2.tryParse(ctx)
    }
}

infix fun <T1: Any, T2: Any> Parser<T1>.to(parser2: Parser<T2>) = PairParser(this, parser2)
infix fun <T: Any> Parser<T>.or(parser2: Parser<T>) = EitherParser(this, parser2)

val <T1: Any, T2: Any> ParseResult<Pair<ParseResult<T1>, ParseResult<T2>>>.first get() = ParseResult(ctx, value.first.value)
val <T1: Any, T2: Any> ParseResult<Pair<ParseResult<T1>, ParseResult<T2>>>.second get() = ParseResult(ctx, value.second.value)

class PredicateParser<T: Any>(private val parser: Parser<T>, private val predicate: (T) -> Boolean): Parser<T> {
    override fun tryParse(ctx: ParserContext): ParseResult<T>? {
        return parser.tryParse(ctx)?.takeIf { result -> predicate(result.value) }
    }
}

fun <T: Any> Parser<T>.withPredicate(predicate: (T) -> Boolean) = PredicateParser(this, predicate)

class RepeatedParser<T: Any>(private val parser: Parser<T>, private val countRange: IntRange) : Parser<List<T>> {
    override fun tryParse(ctx: ParserContext): ParseResult<List<T>>? {
        var ctx = ctx
        val collected = mutableListOf<T>()
        while (true) {
            val result = parser.tryParse(ctx) ?: break
            ctx = result.ctx
            collected.add(result.value)
        }
        return (collected.size in countRange).ifTrue { ParseResult(ctx, collected) }
    }
}


fun <T: Any> Parser<T>.repeated(countRange: IntRange) = RepeatedParser(this, countRange)
fun <T: Any> Parser<T>.repeated(count: Int) = RepeatedParser(this, IntRange(count, count))
fun <T: Any> Parser<T>.oneOrMore() = repeated(IntRange(1, Int.MAX_VALUE))
fun <T: Any> Parser<T>.zeroOrMore() = repeated(IntRange(0, Int.MAX_VALUE))

fun <T: Any, R: Any> ParseResult<T>.map(transform: (T) -> R): ParseResult<R> {
    return ParseResult(this.ctx, transform(this.value))
}

fun <T: Any, R: Any> Parser<T>.map(transform: (T) -> R): Parser<R> {
    val parserT = this
    return object : Parser<R> {
        override fun tryParse(ctx: ParserContext): ParseResult<R>? {
            return parserT.tryParse(ctx)?.map(transform)
        }
    }
}
