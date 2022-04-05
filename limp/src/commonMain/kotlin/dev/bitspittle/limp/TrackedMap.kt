package dev.bitspittle.limp

/**
 * A map that keeps track of what keys were fetched.
 *
 * This allows us to confirm that all options passed into a method were consumed correctly, which will allow us to throw
 * a useful error at runtime if, for example, a user mistyped an argument name.
 */
class TrackedMap<K, V>(private val wrapped: Map<K, V>): Map<K, V> by wrapped {
    private val _accessedKeys = mutableSetOf<K>()
    val accessedKeys: Set<K> = _accessedKeys

    override fun containsKey(key: K): Boolean {
        _accessedKeys.add(key)
        return wrapped.containsKey(key)
    }

    override fun get(key: K): V? {
        _accessedKeys.add(key)
        return wrapped[key]
    }
}