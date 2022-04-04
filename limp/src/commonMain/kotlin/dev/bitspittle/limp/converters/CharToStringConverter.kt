package dev.bitspittle.limp.converters

import dev.bitspittle.limp.Converter

/**
 * Limp doesn't natively support characters - instead, it's just numbers and strings.
 *
 * Therefore, if a character sneaks into a system, say from a natively implemented method, just convert it to a
 * string.
 */
class CharToStringConverter : Converter<String>(String::class) {
    override fun convert(value: Any): String? {
        return (value as? Char)?.toString()
    }
}