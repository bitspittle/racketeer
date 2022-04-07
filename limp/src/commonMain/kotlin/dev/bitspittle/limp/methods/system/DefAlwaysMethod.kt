package dev.bitspittle.limp.methods.system

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method

/**
 * Like [DefMethod] but default overwrite to true.
 */
class DefAlwaysMethod : Method("def!", 0, consumeRest = true) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        return DefMethod().invoke(env, eval, params, options + mapOf("overwrite" to true), rest)
    }
}