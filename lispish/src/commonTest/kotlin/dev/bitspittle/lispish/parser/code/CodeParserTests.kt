package dev.bitspittle.lispish.parser.code

import com.varabyte.truthish.assertThat
import dev.bitspittle.lispish.parser.ParserContext
import dev.bitspittle.lispish.parser.parsers.code.IdentifierParser
import dev.bitspittle.lispish.parser.parsers.combinators.first
import dev.bitspittle.lispish.parser.parsers.combinators.second
import dev.bitspittle.lispish.parser.parsers.combinators.to
import dev.bitspittle.lispish.parser.parsers.text.IntParser
import dev.bitspittle.lispish.parser.parsers.text.WordParser
import kotlin.test.Test

class CodeParserTests {
    @Test
    fun testIdentifierParser() {
        val identifierParser = IdentifierParser()

        identifierParser.tryParse(ParserContext("valid-id5 etc."))!!.let { result ->
            assertThat(result.value).isEqualTo("valid-id5")
        }

        // TODO: Fix these
//        assertThat(identifierParser.tryParse(ParserContext("id.with.invalid.characters"))).isNull()
//        assertThat(identifierParser.tryParse(ParserContext("id--with--too--many--hyphens--in--a--row"))).isNull()
//        assertThat(identifierParser.tryParse(ParserContext("123id-must-start-with-a-letter"))).isNull()
//        assertThat(identifierParser.tryParse(ParserContext("-id-must-start-with-a-letter"))).isNull()
    }
}