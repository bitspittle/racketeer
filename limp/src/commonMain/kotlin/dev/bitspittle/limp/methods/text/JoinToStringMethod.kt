package dev.bitspittle.limp.methods.text

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr

class JoinToStringMethod : Method("join-to-string", 1) {
    override suspend fun invoke(env: Environment, eval: Evaluator, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val list = env.expectConvert<List<Any>>(params[0])
        val separator = options["separator"]?.let { env.expectConvert(it)} ?: ", "
        val format = options["format"]?.let { env.expectConvert<Expr>(it)}
        @Suppress("NAME_SHADOWING")
        return (format?.let { format ->
            list.map { item ->
                env.scoped { // Don't let values defined during the lambda escape
                    env.expectConvert(eval.extend(mapOf("\$it" to item)).evaluate(env, format))
                }
            }
        } ?: list).joinToString(separator)
    }
}