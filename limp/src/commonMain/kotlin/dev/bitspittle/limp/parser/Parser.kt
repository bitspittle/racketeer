package dev.bitspittle.limp.parser

interface Parser<T: Any> {
    fun tryParse(ctx: ParserContext): ParseResult<T>?
}