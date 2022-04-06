package dev.bitspittle.limp

import dev.bitspittle.limp.converters.CompositeConverter
import kotlin.reflect.KClass

abstract class Converter<T: Any>(val toClass: KClass<out T>) {
    abstract fun convert(value: Any): T?

    operator fun <U: Any> plus(other: Converter<U>) = CompositeConverter(this, other)
}
