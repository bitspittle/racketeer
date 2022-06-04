package dev.bitspittle.racketeer.model.card

import kotlinx.serialization.Serializable

@Serializable
data class TraitNames(
    val expendable: String,
    val suspicious: String,
    val swift: String,
)

// Note: Order is intentional. If a card has multiple traits, we want to describe them in this order.
enum class TraitType {
    SWIFT,
    SUSPICIOUS,
    EXPENDABLE,
}
