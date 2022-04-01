package dev.bitspittle.lispish.parser

class ParseResult<T: Any>(val ctx: ParserContext, val value: T)