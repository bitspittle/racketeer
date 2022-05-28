@file:Suppress("UNCHECKED_CAST")

package dev.bitspittle.racketeer.scripting.converters

import dev.bitspittle.limp.Converter
import dev.bitspittle.racketeer.model.serialization.DataValue
import dev.bitspittle.racketeer.model.serialization.asAny

/**
 * Convert a data value to its wrapped value.
 *
 * DataValue wrappers are an implementation detail for serialization / deserialization support.
 * When scripts interact with values that happened to get boxed inside a data value, they don't
 * care about that; they really want to work with the raw value underneath.
 */
class DataValueToAnyConverter : Converter<Any>(Any::class) {
    override fun convert(value: Any): Any? {
        return (value as? DataValue)?.asAny
    }
}
