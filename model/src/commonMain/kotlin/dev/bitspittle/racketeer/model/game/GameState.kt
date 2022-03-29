package dev.bitspittle.racketeer.model.game

import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate

class GameState(
    private val config: GameConfig,
) {
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
    private val _street = mutableListOf<Card>()
    private val _deck = mutableListOf<Card>()
    private val _hand = mutableListOf<Card>()
    private val _discard = mutableListOf<Card>()
    private val _jail = mutableListOf<Card>()

    val shop: List<CardTemplate> = _shop
    val street: List<Card> = _street
    val deck: List<Card> = _deck
    val hand: List<Card> = _hand
    val discard: List<Card> = _discard
    val jail: List<Card> = _jail
}