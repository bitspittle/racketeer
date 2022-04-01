package dev.bitspittle.lispish.parser.parsers.code

import dev.bitspittle.lispish.parser.ParseResult
import dev.bitspittle.lispish.parser.Parser
import dev.bitspittle.lispish.parser.ParserContext
import dev.bitspittle.lispish.parser.parsers.combinators.map
import dev.bitspittle.lispish.parser.parsers.combinators.or
import dev.bitspittle.lispish.parser.parsers.combinators.to
import dev.bitspittle.lispish.parser.parsers.combinators.zeroOrMore
import dev.bitspittle.lispish.parser.parsers.text.LetterOrDigitParser
import dev.bitspittle.lispish.parser.parsers.text.LetterParser
import dev.bitspittle.lispish.parser.parsers.text.MatchCharParser

class IdentifierParser : Parser<String> {
    override fun tryParse(ctx: ParserContext): ParseResult<String>? {
        val initialLetterParser = LetterParser()
        val remainingLettersParser = (LetterOrDigitParser() or MatchCharParser('-')).zeroOrMore()
        return (initialLetterParser to remainingLettersParser)
            .tryParse(ctx)
            ?.map { (initialResult, restResult) ->
                initialResult.value + restResult.value.joinToString("")
            }
    }
}
