package dev.bitspittle.lispish.converters

import dev.bitspittle.lispish.Converter
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class SingletonListToItemConverter<T: Any>(toClass: KClass<T>) : Converter<T>(toClass) {
    override fun convert(value: Any): T? {
        if (value is List<*> && value.size == 1) {
            val singleItem = value[0]
            if (singleItem != null && singleItem::class == toClass) {
                return singleItem as T
            }
        }

        return null
    }
}