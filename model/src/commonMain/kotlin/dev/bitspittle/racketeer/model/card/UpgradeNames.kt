package dev.bitspittle.racketeer.model.card

import kotlinx.serialization.Serializable

@Serializable
data class UpgradeNames(
    val cash: String,
    val influence: String,
    val luck: String,
    val undercover: String,
    val jailbird: String = "Jailbird", // TODO: Force set in game.yaml after discussing with designerz
)

enum class UpgradeType {
    CASH,
    INFLUENCE,
    LUCK,
    JAILBIRD,
    UNDERCOVER,
}