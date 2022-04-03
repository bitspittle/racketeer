package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.Value
import dev.bitspittle.limp.converters.PlaceholderConverter
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.types.ListStrategy

/**
 * Take some number of elements from a list, returning what was taken.
 */
class TakeMethod : Method("take", 2) {
    override fun invoke(env: Environment, params: List<Value>, options: Map<String, Value>, rest: List<Value>): Value {
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

        return Value(
            when (strategy) {
                ListStrategy.FRONT -> list.take(count)
                ListStrategy.BACK -> list.takeLast(count)
                ListStrategy.RANDOM -> list.shuffled(env.random).take(count)
            }
        )
    }
}