package dev.bitspittle.lispish

import kotlin.reflect.KClass

abstract class Converter<T: Any>(val toClass: KClass<out T>) {
    abstract fun convert(value: Any): T?
}

@Suppress("UNCHECKED_CAST")
class Converters {
    private val converters = mutableMapOf<KClass<*>, Converter<*>>()

    fun <T: Any> register(converter: Converter<T>) {
        converters[converter.toClass] = converter
    }

    fun <T: Any> convert(value: Any, toClass: KClass<T>): T? {
        if (value::class == toClass) {
            return value as T
        }

        return converters[toClass]?.convert(value) as T?
    }
}