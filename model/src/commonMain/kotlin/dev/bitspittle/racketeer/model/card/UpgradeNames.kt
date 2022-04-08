package dev.bitspittle.racketeer.model.card

import kotlinx.serialization.Serializable

@Serializable
data class UpgradeNames(
    val cash: String,
    val influence: String,
    val luck: String,
    val patience: String,
)

enum class UpgradeType {
    CASH,
    INFLUENCE,
    LUCK,
    PATIENCE,
}