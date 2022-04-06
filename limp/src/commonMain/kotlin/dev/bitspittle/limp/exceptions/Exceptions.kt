package dev.bitspittle.limp.exceptions

import dev.bitspittle.limp.parser.ParserContext
import dev.bitspittle.limp.types.ExprContext

abstract class LispishException(msg: String, cause: Throwable? = null): Exception(msg, cause)

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
    // NOTE: KEEP THE TEXT LEFT JUSTIFIED, BECAUSE $msg MAY HAVE A NEWLINE IN IT, WHICH SCREWS UP INDENTATION
"""
$msg

Error occurred here:

  ${code.replace("\n", " ").replace("\t", " ")}
  ${" ".repeat(index) + "^".repeat(length)}
"""

class ParseException(val ctx: ParserContext, length: Int, val title: String, cause: Throwable? = null): LispishException(
    createCodeErrorMessage(ctx.text, ctx.startIndex, length, title), cause
) {
    constructor(ctx: ParserContext, msg: String): this(ctx, length = 1, msg)
}

class EvaluationException(val ctx: ExprContext, val title: String, cause: Throwable? = null): LispishException(
    createCodeErrorMessage(ctx.code, ctx.start, ctx.length, title), cause
)
