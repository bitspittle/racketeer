package dev.bitspittle.racketeer.model.card

import kotlinx.serialization.Serializable

@Serializable
data class UpgradeNames(
    val cash: String,
    val influence: String,
    val luck: String,
    val veteran: String,
)

// Note: Order is intentional. If a card has multiple traits, we want to describe them in this order.
enum class UpgradeType {
    CASH,
    INFLUENCE,
    LUCK,
    VETERAN,
}
