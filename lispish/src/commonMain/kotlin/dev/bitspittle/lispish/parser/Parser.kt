package dev.bitspittle.lispish.parser

interface Parser<T: Any> {
    fun tryParse(ctx: ParserContext): ParseResult<T>?
}