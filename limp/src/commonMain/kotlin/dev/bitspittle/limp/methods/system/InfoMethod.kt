package dev.bitspittle.limp.methods.system

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Logger

/**
 * info (Any)
 *
 * Print a value out to the console.
 */
class InfoMethod(private val logger: Logger) : Method("info", 1) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        logger.info(params[0].toString())
        return Unit
    }
}