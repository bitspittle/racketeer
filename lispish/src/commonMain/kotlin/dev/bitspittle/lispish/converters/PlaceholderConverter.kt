package dev.bitspittle.lispish.converters

import dev.bitspittle.lispish.Converter
import dev.bitspittle.lispish.types.Placeholder
import kotlin.reflect.KClass

/**
 * Convert a placeholder value to some target value.
 */
class PlaceholderConverter<T: Any>(private val toValue: T) : Converter<T>(toValue::class) {
    override fun convert(value: Any): T? {
        return if (value === Placeholder) {
            toValue
        } else {
            null
        }
    }
}