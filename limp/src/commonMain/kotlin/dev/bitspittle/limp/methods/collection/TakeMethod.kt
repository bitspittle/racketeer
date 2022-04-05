package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.converters.PlaceholderConverter
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.types.ListStrategy

/**
 * Take some number of elements from a list, returning what was taken.
 */
class TakeMethod : Method("take", 2) {
    override suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val list = env.expectConvert<List<*>>(params[0])
        val count = env.scoped {
            env.addConverter(PlaceholderConverter(list.size))
            env.expectConvert<Int>(params[1])
        }.coerceAtMost(list.size)

        val strategy =
            options["from"]?.let { from ->
                val fromIdentifier = env.expectConvert<Expr.Identifier>(from)
                ListStrategy.values().single { it.name.lowercase() == fromIdentifier.name }
            } ?: ListStrategy.FRONT

        return when (strategy) {
            ListStrategy.FRONT -> list.take(count)
            ListStrategy.BACK -> list.takeLast(count)
            ListStrategy.RANDOM -> list.shuffled(env.random).take(count)
        }
    }
}