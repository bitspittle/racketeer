package dev.bitspittle.limp.converters

import dev.bitspittle.limp.Converter

/**
 * Convert an int value to a range constrained to that integer as both start and end values.
 */
class IntToIntRangeConverter : Converter<IntRange>(IntRange::class) {
    override fun convert(value: Any): IntRange? {
        return (value as? Int)?.let { IntRange(it, it) }
    }
}