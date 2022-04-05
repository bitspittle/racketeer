package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method

/**
 * Take a list and return a sorted copy of it.
 */
class SortedMethod : Method("sorted", 1) {
    override suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        // We can re-use "sort" instead of implementing the behavior a second time
        val copyToSort = env.expectConvert<List<Comparable<Any>>>(params[0]).toMutableList()
        SortMethod().invoke(env, listOf(copyToSort), options)

        return copyToSort
    }
}