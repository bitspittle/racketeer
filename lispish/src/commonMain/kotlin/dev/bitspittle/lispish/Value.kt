package dev.bitspittle.lispish

import kotlin.reflect.KClass

class Value(val wrapped: Any) {
    companion object {
        val Empty = Value(Unit)
        val Placeholder = Value(dev.bitspittle.lispish.types.Placeholder)
    }

    fun <T: Any> into(converters: Converters, toClass: KClass<T>): T? {
        return converters.convert(wrapped, toClass)
    }

    override fun toString(): String {
        return "Value { $wrapped }"
    }
}