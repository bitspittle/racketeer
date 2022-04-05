package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.types.SortOrder
import dev.bitspittle.limp.utils.BinarySearchHelper
import dev.bitspittle.limp.utils.toEnumOrNull

/**
 * Take a list and return a sorted copy of it.
 */
class SortMethod : Method("sort", 1) {
    override suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val list = env.expectConvert<List<Comparable<Any>>>(params[0])

        val order = options["order"]?.let { from ->
            env.expectConvert<Expr.Identifier>(from).toEnumOrNull(SortOrder.values())
        } ?: SortOrder.ASCENDING

        val comparator = options["with"]?.let { comparator -> env.expectConvert<Expr>(comparator) }

        return if (list.isNotEmpty() && comparator != null) {
            // We need to implement sort ourselves because we want an algorithm that's suspendable.
            val sorted = mutableListOf(list[0])
            while (sorted.size < list.size) {
                val binarySearch = BinarySearchHelper(sorted.size)
                val toInsert = list[sorted.size]

                while (true) {
                    val i = binarySearch.mid
                    val currItem = sorted[i]
                    val compare = env.expectConvert<Int>(
                        Evaluator(
                            mapOf(
                                "\$a" to toInsert,
                                "\$b" to currItem,
                            )
                        ).evaluate(env, comparator)
                    )
                    if (compare == 0) {
                        sorted.add(binarySearch.mid, toInsert)
                        break
                    } else if (compare < 0) {
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
        }
    }
}