package dev.bitspittle.limp.parser

class ParseResult<out T: Any>(val ctx: ParserContext, val value: T)