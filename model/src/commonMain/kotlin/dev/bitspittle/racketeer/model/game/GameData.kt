package dev.bitspittle.racketeer.model.game

import dev.bitspittle.racketeer.model.card.CardTemplate
import kotlinx.serialization.Serializable

@Serializable
data class GameData(
    val config: GameConfig,
    val cards: List<CardTemplate>
)