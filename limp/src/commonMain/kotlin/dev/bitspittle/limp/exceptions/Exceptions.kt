package dev.bitspittle.limp.exceptions

import dev.bitspittle.limp.parser.ParserContext

abstract class LispishException(msg: String): Exception(msg)

private fun createCodeErrorMessage(code: String, index: Int, msg: String) = createCodeErrorMessage(code, index, length = 1, msg)

/**
 * Message will look something like:
 *
 * ```
 * Could not blah.
 *
 * Error occurred here:
 *
 * > method arg1 arg2 submethod $var
 *               ^^^^
 * ```
 */
private fun createCodeErrorMessage(code: String, index: Int, length: Int, msg: String) =
    """
        $msg

        Error occurred here:

        > $code
          ${"^".repeat(length).padStart(index)}
    """.trimIndent()

class ParseException(ctx: ParserContext, length: Int, msg: String): LispishException(
    createCodeErrorMessage(ctx.text, ctx.startIndex, length, msg)
) {
    constructor(ctx: ParserContext, msg: String): this(ctx, length = 1, msg)
}

class EvaluationException(code: String, index: Int, msg: String): LispishException(
    createCodeErrorMessage(code, index, msg)
)
