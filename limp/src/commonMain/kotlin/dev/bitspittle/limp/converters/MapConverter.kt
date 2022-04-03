package dev.bitspittle.limp.converters

import dev.bitspittle.limp.Converter
import kotlin.reflect.KClass

/**
 * Register a map of key/value pairs that this converter should handle
 */
class MapConverter<V : Any>(private val items: Map<*, V>, toClass: KClass<out V> = items.values.first()::class) :
    Converter<V>(toClass) {
    override fun convert(value: Any): V? {
        return items[value]
    }
}