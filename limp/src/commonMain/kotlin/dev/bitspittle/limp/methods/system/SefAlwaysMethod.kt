package dev.bitspittle.limp.methods.system

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Logger

/**
 * Like [SetMethod] but default overwrite to true.
 */
class SetAlwaysMethod(private val logger: Logger) : Method("set!", 2) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        return SetMethod(logger).invoke(env, eval, params, options + mapOf("overwrite" to true), rest)
    }
}