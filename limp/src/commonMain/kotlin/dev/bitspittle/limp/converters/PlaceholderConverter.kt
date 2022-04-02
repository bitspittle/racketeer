package dev.bitspittle.limp.converters

import dev.bitspittle.limp.Converter
import dev.bitspittle.limp.types.Placeholder
import dev.bitspittle.limp.utils.ifTrue
import kotlin.reflect.KClass

/**
 * Convert a placeholder value to some target value.
 */
class PlaceholderConverter<T: Any>(private val toValue: T, toClass: KClass<out T> = toValue::class) : Converter<T>(toClass) {
    override fun convert(value: Any): T? {
        return (value === Placeholder).ifTrue { toValue }
    }
}