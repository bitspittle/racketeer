package dev.bitspittle.limp.converters

import dev.bitspittle.limp.Converter
import kotlin.reflect.KClass

/**
 * Convert an int value to a range constrained to that integer as both start and end values.
 */
@Suppress("UNCHECKED_CAST")
class IntRangeToListConverter : Converter<List<Int>>(List::class as KClass<List<Int>>) {
    override fun convert(value: Any): List<Int>? {
        return (value as? IntRange)?.toList()
    }
}