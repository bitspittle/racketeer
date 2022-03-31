package dev.bitspittle.lispish

import kotlin.reflect.KClass

class Value(private val wrapped: Any) {
    companion object {
        val Empty = Value(Unit)
    }

    fun <T: Any> into(converters: Converters, toClass: KClass<T>): T? {
        return converters.convert(wrapped, toClass)
    }
}