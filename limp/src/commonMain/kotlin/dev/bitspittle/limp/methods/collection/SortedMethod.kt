package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.types.SortOrder
import dev.bitspittle.limp.utils.BinarySearchHelper
import dev.bitspittle.limp.utils.toEnum

/**
 * sorted (List) -> List
 *
 * Take a list and return a sorted copy of it.
 */
class SortedMethod : Method("sorted", 1) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val sorted = mutableListOf<Any>()

        val order = options["order"]?.let { from ->
            env.expectConvert<Expr.Identifier>(from).toEnum(SortOrder.values())
        } ?: SortOrder.ASCENDING

        val comparator = options["with"]?.let { comparator -> env.expectConvert<Expr>(comparator) }

        return if (comparator != null) {
            // Keep track of the original indices, because we'll preserve original sort order if necessary
            val sortedPairs = mutableListOf<Pair<Any, Int>>()

            val toSort = env.expectConvert<List<Any>>(params[0]).mapIndexed { i, item -> item to i }
            if (toSort.isNotEmpty()) {
                // We need to implement sort ourselves because we want an algorithm that's suspendable. Sort into a second
                // buffer and then return that one.
                sortedPairs.add(toSort[0])
                while (sortedPairs.size < toSort.size) {
                    val binarySearch = BinarySearchHelper(sortedPairs.size)
                    val toInsert = toSort[sortedPairs.size]

                    while (true) {
                        val i = binarySearch.mid
                        val currItem = sortedPairs[i]
                        var compare = env.expectConvert<Int>(
                            env.scoped { // Don't let values defined during the lambda escape
                                eval.extend(
                                    mapOf(
                                        "\$a" to toInsert.first,
                                        "\$b" to currItem.first,
                                    )
                                ).evaluate(env, comparator)
                            }
                        )
                        if (compare == 0) {
                            // If these values resolve to the same using the current comparator, just preserve the original
                            // sort order
                            compare = toInsert.second.compareTo(currItem.second)
                        }

                        check(compare != 0)
                        if (compare < 0) {
                            if (!binarySearch.goLower()) {
                                sortedPairs.add(binarySearch.mid, toInsert)
                                break
                            }
                        } else {
                            check(compare > 0)
                            if (!binarySearch.goHigher()) {
                                sortedPairs.add(binarySearch.mid + 1, toInsert)
                                break
                            }
                        }
                    }
                }

                // Throw out the original indicies, we don't need them anymore
                sorted.addAll(sortedPairs.map { it.first })
            }
            when (order) {
                SortOrder.ASCENDING -> sorted
                SortOrder.DESCENDING -> sorted.apply { reverse() }
            }
        } else {
            val toSort = env.expectConvert<List<Comparable<Any>>>(params[0])

            when (order) {
                SortOrder.ASCENDING -> toSort.sorted()
                SortOrder.DESCENDING -> toSort.sortedDescending()
            }
        }
    }
}