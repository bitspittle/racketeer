package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method

/**
 * list (Any) (Any) (Any)...
 *
 * Consume all remaining arguments and create a list out of them.
 */
class ListMethod : Method("list", 0, consumeRest = true) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        // Make a copy of the list, as otherwise it will get deleted out from under us
        return rest.toList()
    }
}