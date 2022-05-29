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

// Some upgrades are internal features that just happen to use upgrades as a way to implement them. They should not
// be exposed to users.
fun UpgradeType.isInternal(): Boolean {
    return when (this) {
        UpgradeType.SWIFT -> true
        else -> false
    }
}