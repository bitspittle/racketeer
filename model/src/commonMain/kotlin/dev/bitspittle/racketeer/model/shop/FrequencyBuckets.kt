package dev.bitspittle.racketeer.model.shop

import kotlin.random.Random

/**
 * A class which wraps a list of frequency distributions, which calculates the chance of something ending up in a
 * particular bucket.
 *
 * For example, if the passed in list is [5, 4, 3, 2, 1], that means there's a
 * 5 / 15 (~33% chance) to get the first bucket
 * 4 / 15 (~26% chance) to get the second bucket
 * 3 / 15 (20% chance) to get the third bucket
 * 2 / 15 (~13% chance) to get the fourth bucket
 * 1 / 15 (~6% chance) to get the fifth bucket
 */
class FrequencyBuckets(frequencyDistribution: List<Int>) {
    // Convert a list like [5, 4, 3, 2, 1] into [5, 9, 12, 14, 15] as it's easier to work with later
    private val frequencySums = frequencyDistribution
        .fold(mutableListOf<Int>()) { acc, i -> acc.add((acc.lastOrNull() ?: 0) + i); acc }
        as List<Int>

    /**
     * Given a list of distribution numbers, e.g. 5, 4, 3, and a target (e.g. 7), find the matching bucket.
     *
     */
    private fun findBucket(value: Int): Int {
        require(value in 1 .. frequencySums.last()) { "Invalid value $value passed to FrequencyDistribution of: $frequencySums"}
        return frequencySums.indexOfFirst { i -> value <= i }
    }

    fun pickRandomBucket(random: Random): Int {
        return findBucket(1 + random.nextInt(0, frequencySums.last()))
    }
}
