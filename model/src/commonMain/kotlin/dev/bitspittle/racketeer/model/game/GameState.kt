package dev.bitspittle.racketeer.model.game

import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.model.card.Pile

class GameState(
    private val data: GameData,
) {
    private val config = data.config

    var turn = 0
        private set

    var cash = 0
        private set

    var influence = 0
        private set

    var luck = 0
        private set

    var vp = 0
        private set

    var handSize = config.initialHandSize
        private set

    var shopTier = 0
        private set

    private val _shop = mutableListOf<CardTemplate>()
    private val _deck = config.initialDeck
        .flatMap {  entry ->
            val cardName = entry.substringBeforeLast(' ')
            val count = entry.substringAfterLast(' ', missingDelimiterValue = "").toIntOrNull() ?: 1

            val card = data.cards.single { it.name == cardName }
            List(count) { card.instantiate() }
        }.toMutableList()
    private val _hand = mutableListOf<Card>()

    private val _street = mutableListOf<Card>()
    private val _discard = mutableListOf<Card>()
    private val _jail = mutableListOf<Card>()

    val shop: List<CardTemplate> = _shop

    val deck: Pile = Pile(_deck)
    val street: Pile = Pile(_street)
    val hand: Pile = Pile(_hand)
    val discard: Pile = Pile(_discard)
    val jail: Pile = Pile(_jail)
}