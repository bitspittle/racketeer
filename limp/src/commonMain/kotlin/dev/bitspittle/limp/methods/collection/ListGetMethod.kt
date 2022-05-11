package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method

/**
 * list-get (List) (index: Int) -> Any
 *
 * Take a list and a 0-based index and return the item at that index.
 */
class ListGetMethod : Method("list-get", 2) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val list = env.expectConvert<List<Any>>(params[0])
        val index = env.expectConvert<Int>(params[1])

        return list[index]
    }
}