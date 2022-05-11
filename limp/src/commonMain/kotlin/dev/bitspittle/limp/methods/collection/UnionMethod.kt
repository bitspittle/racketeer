package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method

/**
 * union (List) (List) (List)...
 *
 * Consume all remaining arguments, which should all be lists, and concatenate them into a longer list.
 */
class UnionMethod : Method("union", 0, consumeRest = true) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val listOfLists = rest.map { value -> env.expectConvert<List<Any>>(value) }
        return listOfLists.flatten()
    }
}