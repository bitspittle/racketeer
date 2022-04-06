package dev.bitspittle.limp.converters

import dev.bitspittle.limp.Converter
import kotlin.reflect.KClass

/**
 * Apply some converter to a source list to convert it into a `List<T>`
 *
 * This converter will fail if any of the items in the original list could not be converted.
 */
@Suppress("UNCHECKED_CAST")
class TransformListConverter<T : Any>(private val itemConverter: Converter<T>) :
    Converter<List<T>>(List::class as KClass<out List<T>>) {
    override fun convert(value: Any): List<T>? {
        return (value as? List<Any>)
            ?.mapNotNull { it -> itemConverter.convert(it) }
            ?.takeIf { result -> result.size == value.size }
    }
}