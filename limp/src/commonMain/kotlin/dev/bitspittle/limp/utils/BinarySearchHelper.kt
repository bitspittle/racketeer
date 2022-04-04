package dev.bitspittle.limp.utils

/**
 * Simple class which maintains state of what index you should check next when doing a binary search.
 */
class BinarySearchHelper(size: Int) {
    private var hi = size - 1
    private var low = 0

    val mid get() = ((low + hi) / 2)
    fun goLower(): Boolean {
        return if (hi > low) {
            hi = mid - 1
            true
        } else {
            false
        }
    }

    fun goHigher(): Boolean {
        return if (hi > low) {
            low = mid + 1
            true
        } else {
            false
        }
    }
}