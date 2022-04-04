package dev.bitspittle.limp.methods.text

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.Value
import dev.bitspittle.limp.types.Expr

class JoinToStringMethod : Method("join-to-string", 1) {
    override fun invoke(env: Environment, params: List<Value>, options: Map<String, Value>, rest: List<Value>): Value {
        val list = env.expectConvert<List<Any>>(params[0])
        val separator = options["separator"]?.let { env.expectConvert(it)} ?: ", "
        val format = options["format"]?.let { env.expectConvert<Expr>(it)}
        return Value((format?.let { format ->
            list.map { item ->
                env.expectConvert(Evaluator(mapOf("\$it" to Value(item))).evaluate(env, format))
            }
        } ?: list).joinToString(separator))
    }
}