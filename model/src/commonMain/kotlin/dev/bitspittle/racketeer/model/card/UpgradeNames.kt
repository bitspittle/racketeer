package dev.bitspittle.racketeer.model.card

import kotlinx.serialization.Serializable

@Serializable
data class UpgradeNames(
    val cash: String,
    val influence: String,
    val luck: String,
    val swift: String,
    val veteran: String,
)

enum class UpgradeType {
    CASH,
    INFLUENCE,
    LUCK,
    SWIFT,
    VETERAN,
}