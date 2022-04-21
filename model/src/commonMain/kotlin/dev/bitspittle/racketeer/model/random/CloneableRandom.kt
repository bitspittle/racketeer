package dev.bitspittle.racketeer.model.random

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.random.Random

class CloneableRandomSerializer : KSerializer<CloneableRandom> {
    override val descriptor = CloneableRandom.serializer().descriptor
    override fun serialize(encoder: Encoder, value: CloneableRandom) = encoder.encodeLong(value.seed)
    override fun deserialize(decoder: Decoder): CloneableRandom = CloneableRandom(decoder.decodeLong())
}

/**
 * A [Random]-like class which can be copied, serialized, and essentially rerun.
 * 
 * Our game needs to be able to restore back in time occasionally, and for that case, we want to be able to take a
 * snapshot of our random number generator at various points of time.
 */
@Serializable(with = CloneableRandomSerializer::class)
class CloneableRandom(seed: Long = Random.Default.nextLong()) {
    var seed: Long = seed
        private set

    private fun random() = Random(seed).also { seed = it.nextLong() }

    fun nextInt() = random().nextInt()
    fun nextInt(until: Int) = random().nextInt(until)
    fun nextInt(from: Int, until: Int) = random().nextInt(from, until)

    operator fun invoke() = random()

    fun copy() = CloneableRandom(seed)
}