package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.Value
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.types.SortOrder
import dev.bitspittle.limp.utils.BinarySearchHelper

/**
 * Take a list and return a sorted copy of it.
 */
class SortMethod : Method("sort", 1) {
    override suspend fun invoke(env: Environment, params: List<Value>, options: Map<String, Value>, rest: List<Value>): Value {
        val list = env.expectConvert<List<Comparable<Any>>>(params[0])

        val order = options["order"]?.let { from ->
            val fromIdentifier = env.expectConvert<Expr.Identifier>(from)
            SortOrder.values().single { it.name.lowercase() == fromIdentifier.name }
        } ?: SortOrder.ASCENDING

        val comparator = options["with"]?.let { comparator -> env.expectConvert<Expr>(comparator) }

        return Value(
            if (list.isNotEmpty() && comparator != null) {
                // We need to implement sort ourselves because we want an algorithm that's suspendable.
                val sorted = mutableListOf(list[0])
                while (sorted.size < list.size) {
                    val binarySearch = BinarySearchHelper(sorted.size)
                    val toInsert = list[sorted.size]

                    while(true) {
                        val i = binarySearch.mid
                        val currItem = sorted[i]
                        val compare = env.expectConvert<Int>(
                            Evaluator(
                                mapOf(
                                    "\$l" to Value(toInsert),
                                    "\$r" to Value(currItem),
                                )
                            ).evaluate(env, comparator)
                        )
                        if (compare == 0) {
                            sorted.add(binarySearch.mid, toInsert)
                            break
                        }
                        else if (compare < 0) {
                            if (!binarySearch.goLower()) {
                                sorted.add(binarySearch.mid, toInsert)
                                break
                            }
                        } else {
                            check(compare > 0)
                            if (!binarySearch.goHigher()) {
                                sorted.add(binarySearch.mid + 1, toInsert)
                                break
                            }
                        }
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