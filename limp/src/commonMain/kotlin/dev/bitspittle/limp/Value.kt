package dev.bitspittle.limp

import kotlin.reflect.KClass

class Value(val wrapped: Any) {
    companion object {
        val Empty = Value(Unit)
        val Placeholder = Value(dev.bitspittle.limp.types.Placeholder)
        val False = Value(false)
        val True = Value(true)
    }

    fun <T: Any> into(converters: Converters, toClass: KClass<T>): T? {
        return converters.convert(wrapped, toClass)
    }

    override fun toString(): String {
        return "Value { $wrapped }"
    }
}