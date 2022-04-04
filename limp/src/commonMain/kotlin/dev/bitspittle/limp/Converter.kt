package dev.bitspittle.limp

import kotlin.reflect.KClass

abstract class Converter<T: Any>(val toClass: KClass<out T>) {
    abstract fun convert(value: Any): T?
}
