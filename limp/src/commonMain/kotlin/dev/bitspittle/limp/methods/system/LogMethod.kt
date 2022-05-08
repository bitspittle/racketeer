package dev.bitspittle.limp.methods.system

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Logger

/**
 * log (Any)
 *
 * Print a value out to the console.
 */
class LogMethod(private val logger: Logger) : Method("log", 1) {
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