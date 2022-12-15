package dev.bitspittle.limp

import kotlin.reflect.KClass

/**
 * A class which does type match checking.
 *
 * For simple cases, e.g. `value is Int`, this approach is overkill, but it shines in cases where type erasure falls
 * over, e.g. Kotlin reflection can't tell the difference between a `List<Int>` and a `List<String>` at runtime.
 */
interface TypeChecker<T : Any> {
    val targetClass: KClass<T>
    fun isInstance(value: Any): Boolean
}

@Suppress("UNCHECKED_CAST")
fun <T: Any> TypeChecker<T>.cast(value: Any): T? = if (isInstance(value)) value as T else null

/**
 * A basic type checker which just verifies that `value is T`
 *
 * Works great as long as you're not dealing with generic classes.
 */
class SimpleTypeChecker<T : Any>(override val targetClass: KClass<T>) : TypeChecker<T> {
    override fun isInstance(value: Any): Boolean {
        return this.targetClass.isInstance(value)
                // Hack: Comparable.isInstance(Number|String) returns false in JS for some reason (but not JVM).
                // Casting still works, so we just force special case handling for now.
                // See also: https://youtrack.jetbrains.com/issue/KT-55489
                || (value is Number && targetClass == Comparable::class)
                || (value is String && targetClass == Comparable::class)
    }
}

inline fun <reified T : Any> typeOf() = SimpleTypeChecker(T::class)

/**
 * A type checker that verifies that a target value is not only a list but contains all elements of a certain type.
 */
@Suppress("UNCHECKED_CAST")
class ListTypeChecker<T : Any>(private val elementClass: KClass<T>) : TypeChecker<List<T>> {
    override val targetClass = List::class as KClass<List<T>>
    override fun isInstance(value: Any): Boolean {
        return if (value is List<*>) {
            val elementAsserter = SimpleTypeChecker(elementClass)
            value.all { element -> element != null && elementAsserter.isInstance(element) }
        } else false
    }
}

inline fun <reified T : Any> listTypeOf() = ListTypeChecker(T::class)
