package dev.bitspittle.racketeer.model.game

import dev.bitspittle.racketeer.model.shop.ShopPrices
import kotlinx.serialization.Serializable

@Serializable
data class GameConfig(
    val icons: GameIcons,
    val initialHandSize: Int,
    val initialLuck: Int,
    val shopPrices: ShopPrices,
)