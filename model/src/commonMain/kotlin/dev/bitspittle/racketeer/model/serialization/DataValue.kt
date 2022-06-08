@file:UseSerializers(UuidSerializer::class)

package dev.bitspittle.racketeer.model.serialization

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import dev.bitspittle.racketeer.model.text.quote
import dev.bitspittle.racketeer.model.text.unquote
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object BoolDataValueAsBoolSerializer : KSerializer<DataValue.OfBoolean> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BoolDataValue", PrimitiveKind.BOOLEAN)
    override fun serialize(encoder: Encoder, value: DataValue.OfBoolean) = encoder.encodeBoolean(value.bool)
    override fun deserialize(decoder: Decoder): DataValue.OfBoolean = DataValue.OfBoolean(decoder.decodeBoolean())
}

object IntDataValueAsIntSerializer : KSerializer<DataValue.OfInt> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("IntDataValue", PrimitiveKind.INT)
    override fun serialize(encoder: Encoder, value: DataValue.OfInt) = encoder.encodeInt(value.int)
    override fun deserialize(decoder: Decoder): DataValue.OfInt = DataValue.OfInt(decoder.decodeInt())
}

object StringDataValueAsStringSerializer : KSerializer<DataValue.OfString> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("StringDataValue", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: DataValue.OfString) = encoder.encodeString(value.str)
    override fun deserialize(decoder: Decoder): DataValue.OfString = DataValue.OfString(decoder.decodeString())
}

object IdDataValueAsStringSerializer : KSerializer<DataValue.OfId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("IdDataValue", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: DataValue.OfId) = encoder.encodeString(value.id.toString())
    override fun deserialize(decoder: Decoder): DataValue.OfId = DataValue.OfId(uuidFrom(decoder.decodeString()))
}

/**
 * A sealed class for limiting data values to a controlled range where everything is serializable.
 *
 * This allows us to essentially permit game scripts to create typed data which is almost as flexible
 * as `Any` as far as the designers are concerned, but with the code able to know how to serialize /
 * deserialize such data to / from disk.
 */
@Serializable
sealed class DataValue {
    companion object {
        fun of(value: Any): DataValue {
            return when (value) {
                is Boolean -> OfBoolean(value)
                is Int -> OfInt(value)
                is String -> {
                    // When we load a game, we get ID values as a string
                    val unquoted = value.unquote()
                    try {
                        OfId(uuidFrom(unquoted))
                    } catch (ignored: IllegalArgumentException) {
                        OfString(unquoted)
                    }
                }
                is Uuid -> OfId(value)
                else -> error("Data value does not currently support type \"${value::class.simpleName}\"")
            }
        }
    }

    @Serializable(with = BoolDataValueAsBoolSerializer::class)
    data class OfBoolean(val bool: Boolean) : DataValue()

    @Serializable(with = IntDataValueAsIntSerializer::class)
    data class OfInt(val int: Int) : DataValue()

    @Serializable(with = StringDataValueAsStringSerializer::class)
    data class OfString(val str: String) : DataValue()

    @Serializable(with = IdDataValueAsStringSerializer::class)
    data class OfId(val id: Uuid) : DataValue()
}

val DataValue.asAny: Any get() {
    return when (this) {
        is DataValue.OfBoolean -> this.bool
        is DataValue.OfInt -> this.int
        is DataValue.OfString -> this.str
        is DataValue.OfId -> this.id
    }
}

val DataValue.asText: Any get() {
    return when (this) {
        is DataValue.OfBoolean -> this.bool.toString()
        is DataValue.OfInt -> this.int.toString()
        is DataValue.OfString -> this.str.quote()
        is DataValue.OfId -> this.id.toString().quote()
    }
}