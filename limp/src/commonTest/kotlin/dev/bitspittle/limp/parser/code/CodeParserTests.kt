package dev.bitspittle.limp.parser.code

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import dev.bitspittle.limp.exceptions.ParseException
import dev.bitspittle.limp.parser.ParserContext
import dev.bitspittle.limp.parser.parsers.code.ExprParser
import dev.bitspittle.limp.parser.parsers.code.IdentifierExprParser
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.types.walk
import kotlin.test.Test

class CodeParserTests {
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
    fun testParsingSimpleExpressions() {
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
    }

    @Test
    fun testComment() {
        val exprParser = ExprParser()

        exprParser.tryParse(ParserContext("comments-work # I can write whatever I want!"))!!.let { result ->
            assertThat((result.value as Expr.Identifier).name).isEqualTo("comments-work")
            assertThat(result.ctx.isFinished).isTrue()
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
            assertThat(
                result.value.walk()
                    .map { "${it::class.simpleName}: ${it.ctx.text}" }
            ).containsExactly(
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