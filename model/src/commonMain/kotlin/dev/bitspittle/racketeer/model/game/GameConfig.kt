package dev.bitspittle.racketeer.model.game

import dev.bitspittle.racketeer.model.tier.Tier
import kotlinx.serialization.Serializable

@Serializable
data class GameConfig(
    val title: String,
    val icons: GameIcons,
    val numTurns: Int,
    val initialHandSize: Int,
    val initialLuck: Int,
    val initialDeck: List<String>,
    val cardTypes: List<String>,
    val tiers: List<Tier>,
    val shopPrices: List<Int>,
    val ratingScores: List<Int>,
) {
    init {
        require(shopPrices.size == tiers.size - 1) { "There should be exactly one less entry for shop prices than tiers" }
        require(ratingScores.size == Rating.values().size - 1) { "Too many scores defined for rating values: ${Rating.values().joinToString { it.name }}"}
    }
}