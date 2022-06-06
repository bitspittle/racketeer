package dev.bitspittle.racketeer.model.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
@SerialName("IntRange")
private class IntRangeSurrogate(val start: Int, val endExclusive: Int)

class IntRangeSerializer : KSerializer<IntRange> {
    private val surrogateSerializer = IntRangeSurrogate.serializer()
    override val descriptor = surrogateSerializer.descriptor
    override fun serialize(encoder: Encoder, value: IntRange) {
        encoder.encodeSerializableValue(surrogateSerializer, IntRangeSurrogate(value.first, value.last))
    }
    override fun deserialize(decoder: Decoder): IntRange {
        return decoder.decodeSerializableValue(surrogateSerializer).let { surrogate ->
            IntRange(surrogate.start, surrogate.endExclusive)
        }
    }
}
