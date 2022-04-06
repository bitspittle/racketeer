package dev.bitspittle.limp.methods.system

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method

/**
 * Print a value out to the console, passing it through afterwards, so you can temporarily insert a dbg statement into
 * a chain while experimenting.
 */
class DbgMethod(private val log: (String) -> Unit) : Method("dbg", 1) {
    override suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val message = options["msg"]?.let { env.expectConvert<String>(it) }
        log(buildString {
            append("[DBG] ")
            if (!message.isNullOrBlank()) {
                append(message)
                append(": ")
            }
            append(params[0])
        })

        return params[0]
    }
}