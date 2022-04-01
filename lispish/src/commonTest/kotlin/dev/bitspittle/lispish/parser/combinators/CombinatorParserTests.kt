package dev.bitspittle.lispish.parser.combinators

import com.varabyte.truthish.assertThat
import dev.bitspittle.lispish.parser.ParserContext
import dev.bitspittle.lispish.parser.parsers.combinators.first
import dev.bitspittle.lispish.parser.parsers.combinators.second
import dev.bitspittle.lispish.parser.parsers.combinators.then
import dev.bitspittle.lispish.parser.parsers.text.IntParser
import dev.bitspittle.lispish.parser.parsers.text.WordParser
import kotlin.test.Test

class CombinatorParserTests {
    @Test
    fun testParserPairs() {
        val wordNumberCombo = WordParser() then IntParser()
        wordNumberCombo.tryParse(ParserContext("abc123"))!!.let { result ->
            assertThat(result.first.value).isEqualTo("abc")
            assertThat(result.second.value).isEqualTo(123)
        }
    }

    // TODO: Add more tests
}