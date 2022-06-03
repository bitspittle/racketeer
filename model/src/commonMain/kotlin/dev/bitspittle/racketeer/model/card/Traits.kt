package dev.bitspittle.racketeer.model.card

import kotlinx.serialization.Serializable

@Serializable
data class TraitNames(
    val expendable: String,
    val suspicious: String,
    val swift: String,
)

enum class TraitType {
    EXPENDABLE,
    SUSPICIOUS,
    SWIFT,
}
