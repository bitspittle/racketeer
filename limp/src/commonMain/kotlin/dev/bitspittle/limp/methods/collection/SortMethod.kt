package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.types.SortOrder
import dev.bitspittle.limp.utils.BinarySearchHelper
import dev.bitspittle.limp.utils.toEnum
import dev.bitspittle.limp.utils.toEnumOrNull

/**
 * sort (MutableList)
 *
 * Take a mutable list and sort it in place.
 */
class SortMethod : Method("sort!", 1) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val order = options["order"]?.let { from ->
            env.expectConvert<Expr.Identifier>(from).toEnum(SortOrder.values())
        } ?: SortOrder.ASCENDING

        val comparator = options["with"]?.let { comparator -> env.expectConvert<Expr>(comparator) }

        if (comparator != null) {
            val toSort = env.expectConvert<MutableList<Any>>(params[0])

            // We need to implement sort ourselves because we want an algorithm that's suspendable. Sort into a second
            // buffer and then set ourselves to it.
            val sorted = mutableListOf(toSort[0])
            while (sorted.size < toSort.size) {
                val binarySearch = BinarySearchHelper(sorted.size)
                val toInsert = toSort[sorted.size]

                while (true) {
                    val i = binarySearch.mid
                    val currItem = sorted[i]
                    var compare = env.expectConvert<Int>(
                        env.scoped { // Don't let values defined during the lambda escape
                            eval.extend(
                                mapOf(
                                    "\$a" to toInsert,
                                    "\$b" to currItem,
                                )
                            ).evaluate(env, comparator)
                        }
                    )
                    if (compare == 0) {
                        // If these values resolve to the same using the current comparator, just preserve the original
                        // sort order
                        compare = toSort.indexOf(toInsert).compareTo(toSort.indexOf(currItem))
                    }

                    check(compare != 0)
                    if (compare < 0) {
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

            toSort.clear()

            when (order) {
                SortOrder.ASCENDING -> toSort.addAll(sorted)
                SortOrder.DESCENDING -> toSort.addAll(sorted.reversed())
            }
        } else {
            val toSort = env.expectConvert<MutableList<Comparable<Any>>>(params[0])

            when (order) {
                SortOrder.ASCENDING -> toSort.sort()
                SortOrder.DESCENDING -> toSort.sortDescending()
            }
        }

        return Unit
    }
}