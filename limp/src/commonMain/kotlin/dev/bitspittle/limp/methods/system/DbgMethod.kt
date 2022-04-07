package dev.bitspittle.limp.methods.system

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method

/**
 * Print a value out to the console, passing it through afterwards, so you can temporarily insert a dbg statement into
 * a chain while experimenting.
 */
class DbgMethod(private val log: (String) -> Unit) : Method("dbg", 1) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val message = options["msg"]?.let { env.expectConvert<String>(it) }
        log(buildString {
            append(message.takeUnless { it.isNullOrBlank() } ?: "Debug")
            append(": ")
            append("${params[0]} # ${params[0]::class.simpleName}")
        })

        return params[0]
    }
}