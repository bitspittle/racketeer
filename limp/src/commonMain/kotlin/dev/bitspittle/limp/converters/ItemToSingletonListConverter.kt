package dev.bitspittle.limp.converters

import dev.bitspittle.limp.Converter
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.utils.ifTrue
import kotlin.reflect.KClass

/**
 * A converter for converting a single item into a list of just that item.
 *
 * This is useful for indicating that a method, which normally expects a list of items, would also be OK accepting
 * a single item as well (but as a list).
 *
 * This is not enabled as a default as it may cause hard to understand evaluation errors, but this converter can be
 * temporarily applied by [Method] implementers on a case-by-case basis to help out the ergonomics of some methods that
 * don't care if they're working on a single instance of something or a list of them.
 */
@Suppress("UNCHECKED_CAST")
class ItemToSingletonListConverter<T: Any>(private val itemClass: KClass<T>) : Converter<List<T>>(List::class as KClass<List<T>>) {
    override fun convert(value: Any): List<T>? {
        return (itemClass.isInstance(value)).ifTrue { listOf(value) } as List<T>?
    }
}