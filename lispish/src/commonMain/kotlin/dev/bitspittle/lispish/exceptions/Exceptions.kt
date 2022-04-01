package dev.bitspittle.lispish.exceptions

abstract class LispishException(msg: String): Exception(msg)

/**
 * Message will look something like:
 *
 * ```
 * Could not blah.
 *
 * Error occurred here:
 *
 * > method arg1 arg2 submethod $var
 *               ^
 * ```
 */
private fun createCodeErrorMessage(code: String, index: Int, msg: String) =
    """
        $msg

        Error occurred here:

        > $code
          ${"^".padStart(index)}
    """.trimIndent()

class ParseException(code: String, index: Int, msg: String): LispishException(
    createCodeErrorMessage(code, index, msg)
)

class EvaluationException(code: String, index: Int, msg: String): LispishException(
    createCodeErrorMessage(code, index, msg)
)
