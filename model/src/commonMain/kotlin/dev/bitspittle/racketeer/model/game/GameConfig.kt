package dev.bitspittle.racketeer.model.game

import kotlinx.serialization.Serializable

@Serializable
data class GameConfig(
    val title: String,
    val icons: GameIcons,
    val initialHandSize: Int,
    val initialLuck: Int,
    val initialDeck: List<String>,
    val shopPrices: List<Int>,
    val tierFrequencies: List<Int>
) {
    init {
        require(shopPrices.size == 4)
        require(tierFrequencies.size == 5)
    }
}