package dev.bitspittle.limp.converters

import dev.bitspittle.limp.Converter
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.utils.ifTrue
import kotlin.reflect.KClass

/**
 * A converter for converting a single item into an expression, useful for methods that expect an expression as a
 * parameter.
 */
@Suppress("UNCHECKED_CAST")
class ValueToExprConverter<T: Any>(private val fromClass: KClass<T>) : Converter<Expr>(Expr::class) {
    override fun convert(value: Any): Expr? {
        return (fromClass.isInstance(value)).ifTrue { Expr.Stub(value) }
    }
}