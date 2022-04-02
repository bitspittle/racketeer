package dev.bitspittle.limp

/**
 * A weird "immutable" map that self-destructs its key/value pair when fetched.
 *
 * This allows us to confirm that all options passed into a method are consumed, which will allow us to throw a useful
 * error at runtime if, for example, a user mistyped an argument name, because we'll know it's still in the map.
 * However, for most method implementers, they don't have to worry about this -- they just take their `Map<K, V>` and
 * ask for the option values that they needed.
 */
class SelfDestructingMap<K, V>(private val wrapped: MutableMap<K, V>): Map<K, V> by wrapped {
    override fun get(key: K): V? {
        return wrapped.remove(key)
    }
}