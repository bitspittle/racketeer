package dev.bitspittle.limp.parser.code

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import dev.bitspittle.limp.exceptions.ParseException
import dev.bitspittle.limp.parser.ParserContext
import dev.bitspittle.limp.parser.parsers.code.*
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.types.walk
import kotlin.test.Test

class CodeParserTests {
    private fun Expr.debugLines() = this.walk()
        .map { "${it::class.simpleName}: ${it.ctx.text}" }

    @Test
    fun testIdentifierParser() {
        val identifierParser = IdentifierExprParser()

        identifierParser.tryParse(ParserContext("valid-id123"))!!.let { result ->
            assertThat(result.value.name).isEqualTo("valid-id123")
        }

        identifierParser.tryParse(ParserContext("id-ends-at-whitespace\tetc."))!!.let { result ->
            assertThat(result.value.name).isEqualTo("id-ends-at-whitespace")
        }

        identifierParser.tryParse(ParserContext("id-ends-at-rparens)"))!!.let { result ->
            assertThat(result.value.name).isEqualTo("id-ends-at-rparens")
        }

        assertThat(identifierParser.tryParse(ParserContext("(hello)"))).isNull()
        assertThat(identifierParser.tryParse(ParserContext(""))).isNull()

        for (invalidChar in listOf('\'', '~', '`')) {
            assertThat(identifierParser.tryParse(ParserContext("${invalidChar}not-consumed-as-id"))).isNull()

            assertThrows<ParseException> {
                identifierParser.tryParse(ParserContext("invalid${invalidChar}char"))
            }
        }
    }

    @Test
    fun testTextParsing() {
        val textParser = TextExprParser()

        textParser.tryParse(ParserContext("\"this is some text alright\""))!!.let { result ->
            assertThat(result.value.text).isEqualTo("this is some text alright")
        }

        textParser.tryParse(ParserContext(""""I was impressed. \"You supported nested quotes?\", I asked.""""))!!
            .let { result ->
                assertThat(result.value.text).isEqualTo("I was impressed. \"You supported nested quotes?\", I asked.")
            }

        assertThat(textParser.tryParse(ParserContext("not a string without a leading quote\""))).isNull()
        assertThat(textParser.tryParse(ParserContext("\"not a string without a trailing quote"))).isNull()
    }

    @Test
    fun testDeferredParsing() {
        val deferredParser = DeferredExprParser()

        deferredParser.tryParse(ParserContext("'symbol"))!!.let { result ->
            assertThat((result.value.expr as Expr.Identifier).name).isEqualTo("symbol")
        }

        deferredParser.tryParse(ParserContext("'(a b c)"))!!.let { result ->
            assertThat(result.value.expr.ctx.text).isEqualTo("(a b c)")
        }

        deferredParser.tryParse(ParserContext("'''nested"))!!.let { result ->
            assertThat(result.value.debugLines()).containsExactly(
                "Deferred: '''nested",
                "Deferred: ''nested",
                "Deferred: 'nested",
                "Identifier: nested",
            ).inOrder()
        }

        assertThat(deferredParser.tryParse(ParserContext("not'deferred"))).isNull()
    }

    @Test
    fun testOptionParsing() {
        val optionParser = OptionExprParser()

        optionParser.tryParse(ParserContext("--pos"))!!.let { result ->
            assertThat((result.value.identifier).name).isEqualTo("pos")
        }

        optionParser.tryParse(ParserContext("-----not-great-but-allowed"))!!.let { result ->
            assertThat((result.value.identifier).name).isEqualTo("---not-great-but-allowed")
        }

        assertThat(optionParser.tryParse(ParserContext("-pos"))).isNull()
        assertThat(optionParser.tryParse(ParserContext("--"))).isNull()
        assertThat(optionParser.tryParse(ParserContext("--'deferred"))).isNull()
    }

    @Test
    fun testParsingExpressions() {
        val exprParser = ExprParser()

        exprParser.tryParse(ParserContext("chain \"of\" 4 identifiers"))!!.let { result ->
            (result.value as Expr.Chain).let { chain ->
                assertThat(chain.exprs).hasSize(4)
                assertThat((chain.exprs[0] as Expr.Identifier).name).isEqualTo("chain")
                assertThat((chain.exprs[1] as Expr.Text).text).isEqualTo("of")
                assertThat((chain.exprs[2] as Expr.Number).number).isEqualTo(4)
                assertThat((chain.exprs[3] as Expr.Identifier).name).isEqualTo("identifiers")
            }
            assertThat(result.ctx.isFinished).isTrue()
        }

        exprParser.tryParse(ParserContext("123"))!!.let { result ->
            assertThat((result.value as Expr.Number).number).isEqualTo(123)
            assertThat(result.ctx.isFinished).isTrue()
        }

        exprParser.tryParse(ParserContext("trailing-space-is-ignored    "))!!.let { result ->
            assertThat((result.value as Expr.Identifier).name).isEqualTo("trailing-space-is-ignored")
            assertThat(result.ctx.isFinished).isTrue()
        }

        exprParser.tryParse(ParserContext("   preceding-space-is-ignored"))!!.let { result ->
            assertThat((result.value as Expr.Identifier).name).isEqualTo("preceding-space-is-ignored")
            assertThat(result.ctx.isFinished).isTrue()
        }

        // Expressions don't care about being broken up into multiple lines
        exprParser.tryParse(ParserContext("a b c\nd e f"))!!.let { result ->
            assertThat(result.value.debugLines()).containsExactly(
                "Chain: a b c\nd e f",
                "Identifier: a",
                "Identifier: b",
                "Identifier: c",
                "Identifier: d",
                "Identifier: e",
                "Identifier: f",
            ).inOrder()
        }

        // A chain of blocks
        exprParser.tryParse(ParserContext("(a) (b) (c)"))!!.let { result ->
            assertThat(result.value.debugLines()).containsExactly(
                "Chain: (a) (b) (c)",
                "Block: (a)",
                "Identifier: a",
                "Block: (b)",
                "Identifier: b",
                "Block: (c)",
                "Identifier: c",
            ).inOrder()
        }
    }

    @Test
    fun testComment() {
        val exprParser = ExprParser()

        exprParser.tryParse(ParserContext("comments-work #### I can write whatever I want!"))!!.let { result ->
            assertThat((result.value as Expr.Identifier).name).isEqualTo("comments-work")
            assertThat(result.ctx.isFinished).isTrue()
        }

        exprParser.tryParse(ParserContext("line1 # Comment ends at newline\nline2"))!!.let { result ->
            (result.value as Expr.Chain).exprs.let { exprs ->
                assertThat(exprs).hasSize(2)
                assertThat((exprs[0] as Expr.Identifier).name).isEqualTo("line1")
                assertThat((exprs[1] as Expr.Identifier).name).isEqualTo("line2")
            }
        }

        exprParser.tryParse(ParserContext("\"String # chars are not consumed as a comment\""))!!.let { result ->
            assertThat((result.value as Expr.Text).text).isEqualTo("String # chars are not consumed as a comment")
        }

        exprParser.tryParse(ParserContext("1 2 3 4 # comments work in chains too"))!!.let { result ->
            (result.value as Expr.Chain).let { chain ->
                assertThat(chain.exprs).hasSize(4)
                assertThat((chain.exprs[0] as Expr.Number).number).isEqualTo(1)
                assertThat((chain.exprs[1] as Expr.Number).number).isEqualTo(2)
                assertThat((chain.exprs[2] as Expr.Number).number).isEqualTo(3)
                assertThat((chain.exprs[3] as Expr.Number).number).isEqualTo(4)
            }
            assertThat(result.ctx.isFinished).isTrue()
        }

        assertThat(exprParser.tryParse(ParserContext("# just a comment"))).isNull()
    }

    @Test
    fun testBlockParsing() {
        val exprParser = ExprParser()

        exprParser.tryParse(ParserContext("(1) (hi) (\"there\")"))!!.let { result ->
            (result.value as Expr.Chain).let { chain ->
                assertThat(chain.exprs).hasSize(3)
                assertThat(((chain.exprs[0] as Expr.Block).expr as Expr.Number).number).isEqualTo(1)
                assertThat(((chain.exprs[1] as Expr.Block).expr as Expr.Identifier).name).isEqualTo("hi")
                assertThat(((chain.exprs[2] as Expr.Block).expr as Expr.Text).text).isEqualTo("there")
            }
        }

        exprParser.tryParse(ParserContext("(outer blocks are ok)"))!!.let { result ->
            (result.value as Expr.Block).let { block ->
                (block.expr as Expr.Chain).let { chain ->
                    assertThat(chain.exprs).hasSize(4)
                }
            }
        }

         exprParser.tryParse(ParserContext("(((nested blocks) are) totally (fine))"))!!.let { result ->
            assertThat(result.value.debugLines()).containsExactly(
                "Block: (((nested blocks) are) totally (fine))",
                "Chain: ((nested blocks) are) totally (fine)",
                "Block: ((nested blocks) are)",
                "Chain: (nested blocks) are",
                "Block: (nested blocks)",
                "Chain: nested blocks",
                "Identifier: nested",
                "Identifier: blocks",
                "Identifier: are",
                "Identifier: totally",
                "Block: (fine)",
                "Identifier: fine",
            ).inOrder()
        }

        assertThrows<ParseException> {
            exprParser.tryParse(ParserContext("(missing right paren # comment added to make things interesting"))
        }

        assertThrows<ParseException> {
            // Use Expr.parse for this last one, it's more strict. ExprParser itself would just
            // consume everything up to the right paren and leave it there
            Expr.parse("missing left paren)")
        }
    }
}