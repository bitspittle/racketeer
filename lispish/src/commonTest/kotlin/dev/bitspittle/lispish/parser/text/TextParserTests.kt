package dev.bitspittle.lispish.parser.text

import com.varabyte.truthish.assertThat
import dev.bitspittle.lispish.parser.ParserContext
import dev.bitspittle.lispish.parser.parsers.combinators.oneOrMore
import dev.bitspittle.lispish.parser.parsers.text.*
import kotlin.test.Test

class TextParserTests {
    @Test
    fun testAnyCharParser() {
        val anyCharParser = AnyCharParser()
        var ctx = ParserContext("Ab3 \t!")

        anyCharParser.tryParse(ctx)!!.let { result ->
            assertThat(result.value).isEqualTo('A')
            assertThat(result.ctx.remaining).isEqualTo("b3 \t!")
            ctx = result.ctx
        }

        anyCharParser.tryParse(ctx)!!.let { result ->
            assertThat(result.value).isEqualTo('b')
            assertThat(result.ctx.remaining).isEqualTo("3 \t!")
            ctx = result.ctx
        }

        anyCharParser.tryParse(ctx)!!.let { result ->
            assertThat(result.value).isEqualTo('3')
            assertThat(result.ctx.remaining).isEqualTo(" \t!")
            ctx = result.ctx
        }

        anyCharParser.tryParse(ctx)!!.let { result ->
            assertThat(result.value).isEqualTo(' ')
            assertThat(result.ctx.remaining).isEqualTo("\t!")
            ctx = result.ctx
        }

        anyCharParser.tryParse(ctx)!!.let { result ->
            assertThat(result.value).isEqualTo('\t')
            assertThat(result.ctx.remaining).isEqualTo("!")
            ctx = result.ctx
        }

        anyCharParser.tryParse(ctx)!!.let { result ->
            assertThat(result.value).isEqualTo('!')
            assertThat(result.ctx.remaining).isEqualTo("")
            ctx = result.ctx
        }

        assertThat(anyCharParser.tryParse(ctx)).isNull()
    }

    @Test
    fun testLetterParser() {
        val letterParser = LetterParser()
        var ctx = ParserContext("Ab3")

        letterParser.tryParse(ctx)!!.let { result ->
            assertThat(result.value).isEqualTo('A')
            assertThat(result.ctx.remaining).isEqualTo("b3")
            ctx = result.ctx
        }

        letterParser.tryParse(ctx)!!.let { result ->
            assertThat(result.value).isEqualTo('b')
            assertThat(result.ctx.remaining).isEqualTo("3")
            ctx = result.ctx
        }

        assertThat(letterParser.tryParse(ctx)).isNull()
    }

    @Test
    fun testDigitParser() {
        val digitParser = DigitParser()
        var ctx = ParserContext("12!")

        digitParser.tryParse(ctx)!!.let { result ->
            assertThat(result.value).isEqualTo(1)
            assertThat(result.ctx.remaining).isEqualTo("2!")
            ctx = result.ctx
        }

        digitParser.tryParse(ctx)!!.let { result ->
            assertThat(result.value).isEqualTo(2)
            assertThat(result.ctx.remaining).isEqualTo("!")
            ctx = result.ctx
        }

        assertThat(digitParser.tryParse(ctx)).isNull()
    }

    @Test
    fun testTextParser() {
        val textParser = TextParser()
        var ctx = ParserContext("Hello123 World")

        textParser.tryParse(ctx)!!.let { result ->
            assertThat(result.value).isEqualTo("Hello123")
            assertThat(result.ctx.remaining).isEqualTo(" World")
            ctx = result.ctx
        }

        assertThat(textParser.tryParse(ctx)).isNull()
    }

    @Test
    fun testWordParser() {
        val wordParser = WordParser()
        var ctx = ParserContext("Hello123 World")

        wordParser.tryParse(ctx)!!.let { result ->
            assertThat(result.value).isEqualTo("Hello")
            assertThat(result.ctx.remaining).isEqualTo("123 World")
            ctx = result.ctx
        }

        assertThat(wordParser.tryParse(ctx)).isNull()
    }

    @Test
    fun testIntParser() {
        val intParser = IntParser()

        // Positive number
        run {
            var ctx = ParserContext("123abc")

            intParser.tryParse(ctx)!!.let { result ->
                assertThat(result.value).isEqualTo(123)
                assertThat(result.ctx.remaining).isEqualTo("abc")
                ctx = result.ctx
            }

            assertThat(intParser.tryParse(ctx)).isNull()
        }

        // Negative number
        run {
            var ctx = ParserContext("-321xyz")

            intParser.tryParse(ctx)!!.let { result ->
                assertThat(result.value).isEqualTo(-321)
                assertThat(result.ctx.remaining).isEqualTo("xyz")
                ctx = result.ctx
            }

            assertThat(intParser.tryParse(ctx)).isNull()
        }

        // Double dashes ignored
        run {
            val ctx = ParserContext("--321")
            assertThat(intParser.tryParse(ctx)).isNull()
        }
    }

    @Test
    fun testCharEater() {
        val charEater = EatCharParser('*')

        charEater.tryParse(ParserContext("***---"))!!.let { result ->
            assertThat(result.ctx.remaining).isEqualTo("**---")
        }

        charEater.oneOrMore().tryParse(ParserContext("***---"))!!.let { result ->
            assertThat(result.ctx.remaining).isEqualTo("---")
        }

        assertThat(charEater.tryParse(ParserContext("---***"))).isNull()
    }

    @Test
    fun testTextEater() {
        val textEater = EatTextParser("Hello Joe!")

        textEater.tryParse(ParserContext("Hello Joe!"))!!.let { result ->
            assertThat(result.ctx.remaining).isEmpty()
        }

        assertThat(textEater.tryParse(ParserContext("Hello Jane!"))).isNull()

        textEater.tryParse(ParserContext("Hello Joe! Hello Jane!"))!!.let { result ->
            assertThat(result.ctx.remaining).isEqualTo(" Hello Jane!")
        }
    }

    @Test
    fun testWhitespaceEater() {
        val whitespaceEater = EatAllWhitespaceParser()

        assertThat(whitespaceEater.tryParse(ParserContext("No whitespace"))).isNull()
        whitespaceEater.tryParse(ParserContext("\t\t \n  \n\tLots of whitespace"))!!.let { result ->
            assertThat(result.ctx.remaining).isEqualTo("Lots of whitespace")
        }
    }
}