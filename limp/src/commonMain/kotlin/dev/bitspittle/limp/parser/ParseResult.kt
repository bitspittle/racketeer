package dev.bitspittle.limp.parser

class ParseResult<T: Any>(val ctx: ParserContext, val value: T)