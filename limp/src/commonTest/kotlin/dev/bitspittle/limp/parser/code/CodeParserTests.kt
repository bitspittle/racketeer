package dev.bitspittle.limp.parser.code

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import dev.bitspittle.limp.exceptions.ParseException
import dev.bitspittle.limp.parser.ParserContext
import dev.bitspittle.limp.parser.parsers.code.IdentifierExprParser
import kotlin.test.Test

class CodeParserTests {
    @Test
    fun testIdentifierParser() {
        val identifierParser = IdentifierExprParser()

        identifierParser.tryParse(ParserContext("valid-id123"))!!.let { result ->
            assertThat(result.value.identifier).isEqualTo("valid-id123")
        }

        identifierParser.tryParse(ParserContext("id-ends-at-whitespace\tetc."))!!.let { result ->
            assertThat(result.value.identifier).isEqualTo("id-ends-at-whitespace")
        }

        identifierParser.tryParse(ParserContext("id-ends-at-rparens)"))!!.let { result ->
            assertThat(result.value.identifier).isEqualTo("id-ends-at-rparens")
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