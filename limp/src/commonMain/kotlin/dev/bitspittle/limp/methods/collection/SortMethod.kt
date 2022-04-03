package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.Value
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.types.SortOrder

/**
 * Take a list and return a sorted copy of it.
 */
class SortMethod : Method("sort", 1) {
    override fun invoke(env: Environment, params: List<Value>, options: Map<String, Value>, rest: List<Value>): Value {
        val list = env.expectConvert<List<Comparable<Any>>>(params[0])

        val order = options["order"]?.let { from ->
            val fromIdentifier = env.expectConvert<Expr.Identifier>(from)
            SortOrder.values().single { it.name.lowercase() == fromIdentifier.name }
        } ?: SortOrder.ASCENDING

        val comparator = options["with"]?.let { comparator -> env.expectConvert<Expr>(comparator) }

        return Value(
            if (comparator != null) {
                val sorted = list.sortedWith { a, b ->
                    env.scoped {
                        env.storeValue("\$l", Value(a))
                        env.storeValue("\$r", Value(b))
                        env.expectConvert(Evaluator().evaluate(env, comparator))
                    }
                }
                when (order) {
                    SortOrder.ASCENDING -> sorted
                    SortOrder.DESCENDING -> sorted.reversed()
                }
            } else {
                when (order) {
                    SortOrder.ASCENDING -> list.sorted()
                    SortOrder.DESCENDING -> list.sortedDescending()
                }
        })
    }
}