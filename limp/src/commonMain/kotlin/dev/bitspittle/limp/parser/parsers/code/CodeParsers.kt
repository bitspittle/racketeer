package dev.bitspittle.limp.parser.parsers.code

import dev.bitspittle.limp.exceptions.ParseException
import dev.bitspittle.limp.parser.ParseResult
import dev.bitspittle.limp.parser.Parser
import dev.bitspittle.limp.parser.ParserContext
import dev.bitspittle.limp.parser.parsers.combinators.*
import dev.bitspittle.limp.parser.parsers.text.*

private val ALLOWED_SYMBOLS = listOf('$', '_', '%', '+', '-', '*', '/', '|', '&', '.', '=', '<', '>')
private val END_BOUNDARY_CHARS = listOf(')')

class EatEndBoundaryParser : Parser<Unit> {
    override fun tryParse(ctx: ParserContext): ParseResult<Unit>? {
        val parserList = END_BOUNDARY_CHARS.map { MatchCharParser(it) } + listOf(WhitespaceParser(), EndOfTextParser())
        return EatIfMatchAnyParser(*parserList.toTypedArray()).tryParse(ctx)
    }
}

class IdentifierParser : Parser<String> {
    private fun isAllowedChar(c: Char): Boolean = c.isLetterOrDigit() || c in ALLOWED_SYMBOLS

    override fun tryParse(ctx: ParserContext): ParseResult<String>? {
        val firstLetterParser = AnyCharParser().withPredicate(::isAllowedChar)
        // If the first letter matches, we should consume the rest of the identifier greedily. If it ends up containing
        // invalid characters, instead of stopping at that point, just eat them up now and throw an error message
        // below.
        val remainingLettersParser = NonWhitespaceParser().withPredicate { c -> c !in END_BOUNDARY_CHARS }.zeroOrMore()

        return (firstLetterParser then remainingLettersParser)
            .tryParse(ctx)
            ?.map { (initialResult, remainingResult) ->
                val identifier = initialResult.value + remainingResult.value.joinToString("")
                if (!identifier.all(::isAllowedChar)) {
                    throw ParseException(
                        ctx,
                        identifier.length,
                        "Invalid identifier \"$identifier\" contains invalid characters.\n\nAllowed: letters, numbers, and $ALLOWED_SYMBOLS"
                    )
                }
                identifier
            }

//            val remainingLettersParser =
//                NonWhitespaceParser().withPredicate { c -> c !in END_BOUNDARY_CHARS }.zeroOrMore()
//            (initialLetterParser to remainingLettersParser)
//        }
//
//        return (initialLetterParser to remainingLettersParser)
////            .tryParse(ctx)
////            ?.map { (initialResult, restResult) ->

////            }
//        "POOP"
    }
}
