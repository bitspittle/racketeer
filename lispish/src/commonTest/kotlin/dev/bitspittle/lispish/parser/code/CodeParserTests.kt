package dev.bitspittle.lispish.parser.code

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import dev.bitspittle.lispish.exceptions.ParseException
import dev.bitspittle.lispish.parser.ParserContext
import dev.bitspittle.lispish.parser.parsers.code.IdentifierParser
import kotlin.test.Test

class CodeParserTests {
    @Test
    fun testIdentifierParser() {
        val identifierParser = IdentifierParser()

        identifierParser.tryParse(ParserContext("valid-id123"))!!.let { result ->
            assertThat(result.value).isEqualTo("valid-id123")
        }

        identifierParser.tryParse(ParserContext("id-ends-at-whitespace\tetc."))!!.let { result ->
            assertThat(result.value).isEqualTo("id-ends-at-whitespace")
        }

        identifierParser.tryParse(ParserContext("id-ends-at-rparens)"))!!.let { result ->
            assertThat(result.value).isEqualTo("id-ends-at-rparens")
        }

        assertThat(identifierParser.tryParse(ParserContext("(hello)"))).isNull()
        assertThat(identifierParser.tryParse(ParserContext(""))).isNull()

        for (invalidChar in listOf('\'', '~', '`', '#')) {
            assertThat(identifierParser.tryParse(ParserContext("${invalidChar}not-consumed-as-id"))).isNull()

            assertThrows<ParseException> {
                identifierParser.tryParse(ParserContext("invalid${invalidChar}char"))
            }
        }
    }
}