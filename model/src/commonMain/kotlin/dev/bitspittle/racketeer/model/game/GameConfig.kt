package dev.bitspittle.racketeer.model.game

import dev.bitspittle.racketeer.model.tier.Tier
import kotlinx.serialization.Serializable

@Serializable
data class GameConfig(
    val title: String,
    val icons: GameIcons,
    val initialHandSize: Int,
    val initialLuck: Int,
    val initialDeck: List<String>,
    val tiers: List<Tier>,
    val shopPrices: List<Int>
) {
    init {
        require(shopPrices.size == tiers.size - 1)
    }
}