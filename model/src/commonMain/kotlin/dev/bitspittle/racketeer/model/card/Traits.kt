package dev.bitspittle.racketeer.model.card

import kotlinx.serialization.Serializable

@Serializable
data class TraitNames(
    val swift: String,
)

enum class TraitType {
    SWIFT,
}
