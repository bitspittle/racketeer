package dev.bitspittle.racketeer.model.game

import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.model.card.UpgradeNames
import dev.bitspittle.racketeer.model.tier.Tier
import kotlinx.serialization.Serializable

@Serializable
data class GameData(
    val title: String,
    val icons: GameIcons,
    val numTurns: Int,
    val initialHandSize: Int,
    val initialLuck: Int,
    val initialDeck: List<String>,
    val cardTypes: List<String>,
    val upgradeNames: UpgradeNames,
    val tiers: List<Tier>,
    val shopPrices: List<Int>,
    val ratingScores: List<Int>,
    val cards: List<CardTemplate>,
) {
    init {
        require(shopPrices.size == tiers.size - 1) { "There should be exactly one less entry for shop prices than tiers" }
        require(ratingScores.size == Rating.values().size - 1) {
            "Too many scores defined for rating values: ${
                Rating.values().joinToString { it.name }
            }"
        }

        cards.forEach { card ->
            require(card.types.isNotEmpty()) { "Card named \"${card.name}\" needs to have a type." }
            card.types.forEach { type ->
                if (cardTypes.count { it.compareTo(type, ignoreCase = true) == 0 } == 0) {
                    error("The card named \"${card.name}\" has an invalid type \"$type\" defined on it. Should be one of: $cardTypes")
                }
            }
        }
        cards.groupBy { it.name }.let { namedCards ->
            namedCards.forEach { (name, cards) ->
                if (cards.size > 1) {
                    error("The card name \"$name\" has duplicate entries")
                }
            }
        }
    }
}