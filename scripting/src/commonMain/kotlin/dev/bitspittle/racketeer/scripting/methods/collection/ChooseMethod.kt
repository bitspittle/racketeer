package dev.bitspittle.racketeer.scripting.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.converters.IntToIntRangeConverter
import dev.bitspittle.limp.converters.PlaceholderConverter
import dev.bitspittle.limp.types.Logger

interface ChooseHandler {
    /**
     * Return a subset of the input [list] based on the requested [range].
     *
     * If you need to abort this query, just throw an exception.
     */
    suspend fun query(prompt: String?, list: List<Any>, range: IntRange): List<Any>
}

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
        val list = env.expectConvert<List<Any>>(params[0])
        val range = env.scoped {
            env.addConverter(PlaceholderConverter(1 .. Int.MAX_VALUE))
            env.addConverter(IntToIntRangeConverter())
            env.expectConvert<IntRange>(params[1])
        }

        if (range.first <= 0 && range.last <= 0) {
            logger.warn("Requested choosing no items at all. Was this intentional? Did you forget to use a `..` operator?")
            return listOf<Any>()
        }

        if (list.isEmpty() && range.first <= 0) {
            return listOf<Any>()
        }

        if (range.first > list.size) throw IllegalArgumentException("Requested choosing ${range.first} item(s) from a list that only has ${list.size} item(s) in it.")

        val response = chooseHandler.query(prompt, list, range)
        if (response.size !in range) throw IllegalStateException("PLEASE REPORT THIS BUG! Internal code returned a list of size ${response.size} despite being told it must return one within ${range.first} to ${range.last} items.")

        // Make a copy in case the external code tries to give us back the same list, because we can't guarantee that
        // the passed in one won't change under us later
        return response.takeIf { it !== list } ?: response.toList()
    }
}
