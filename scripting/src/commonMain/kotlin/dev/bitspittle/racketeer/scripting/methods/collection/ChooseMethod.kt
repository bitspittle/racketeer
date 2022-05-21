package dev.bitspittle.racketeer.scripting.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.converters.IntToIntRangeConverter
import dev.bitspittle.limp.converters.PlaceholderConverter
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.types.Logger
import dev.bitspittle.racketeer.scripting.types.CancelPlayException

interface ChooseHandler {
    /**
     * Return a subset of the input [list] based on the requested [range].
     *
     * If you need to abort this query, return null. It is an error to do this is [requiredChoice] is set to
     * true.
     */
    suspend fun query(prompt: String?, list: List<Any>, range: IntRange, requiredChoice: Boolean): List<Any>?
}

class FormattedItem(val wrapped: Any, val displayText: String?, val extraText: String?)

/**
 * Ask a user to choose some values.
 *
 * This method is mostly a shell which delegates to a caller to fill out!
 */
class ChooseMethod(private val logger: Logger, private val chooseHandler: ChooseHandler) : Method("choose", 2) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val prompt = options["prompt"]?.let { env.expectConvert<String>(it) }
        val format = options["format"]?.let { env.expectConvert<Expr>(it)}
        val requiredChoice = env.scoped {
            env.addConverter(PlaceholderConverter(true))
            options["required"]?.let { env.expectConvert(it) }
        } ?: false

        val list = env.expectConvert<List<Any>>(params[0])
        val range = env.scoped {
            env.addConverter(PlaceholderConverter(1 .. Int.MAX_VALUE))
            env.addConverter(IntToIntRangeConverter())
            env.expectConvert<IntRange>(params[1])
        }

        if (range.first <= 0 && range.last <= 0) {
            logger.debug("Requested choosing no items at all. Was this intentional? Did you forget to use a `..` operator?")
            return listOf<Any>()
        }

        if (list.isEmpty() && range.first <= 0) {
            return listOf<Any>()
        }

        if (range.first > list.size) throw IllegalArgumentException("Requested choosing ${range.first} item(s) from a list that only has ${list.size} item(s) in it.")

        val listFormatted = if (format != null) {
            list.map { item ->
                env.scoped { // Don't let values defined during the lambda escape
                    val parts = eval.extend(mapOf("\$it" to item)).evaluate(env, format).toString().split(';', limit = 2).map { it.takeIf { it.isNotBlank() } }
                    val (displayText, extraText) = if (parts.size == 1)
                        listOf(parts[0], null)
                    else listOf(parts[0], parts[1])
                    FormattedItem(item, displayText, extraText)
                }
            }
        } else list

        val response = chooseHandler.query(prompt, listFormatted, range, requiredChoice)
            // Return the original item in case we wrapped it with a custom formatter
            ?.map { item -> if (item is FormattedItem) item.wrapped else item }
            ?: run {
                if (requiredChoice) {
                    throw IllegalStateException("PLEASE REPORT THIS BUG! Internal code is not respecting the designers requirement that the user has to choose a value")
                } else {
                    throw CancelPlayException("User canceled making a choice.")
                }
            }

        if (response.size !in range) throw IllegalStateException("PLEASE REPORT THIS BUG! Internal code returned a list of size ${response.size} despite being told it must return one within ${range.first} to ${range.last} items.")

        // Make a copy in case the external code tries to give us back the same list, because we can't guarantee that
        // the passed in one won't change under us later
        return response.takeIf { it !== list } ?: response.toList()
    }
}
