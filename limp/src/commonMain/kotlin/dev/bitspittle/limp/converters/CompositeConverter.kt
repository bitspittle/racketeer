package dev.bitspittle.limp.converters

import dev.bitspittle.limp.Converter

/**
 * A chain of converters, to allow for building multi-step transformations
 *
 * See also: [Converter.plus]
 */
class CompositeConverter<T : Any>(
    private val converter1: Converter<out Any>,
    private val converter2: Converter<T>
) : Converter<T>(converter2.toClass) {
    override fun convert(value: Any): T? {
        return converter1.convert(value)?.let { converter2.convert(it) }
    }
}
