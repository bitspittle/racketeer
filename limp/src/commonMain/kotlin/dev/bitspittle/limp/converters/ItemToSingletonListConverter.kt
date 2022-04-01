package dev.bitspittle.limp.converters

import dev.bitspittle.limp.Converter
import dev.bitspittle.limp.utils.ifTrue
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class ItemToSingletonListConverter<T: Any>(private val itemClass: KClass<T>) : Converter<List<T>>(List::class as KClass<List<T>>) {
    override fun convert(value: Any): List<T>? {
        return (value::class == itemClass).ifTrue { listOf(value) } as List<T>?
    }
}