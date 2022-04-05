package dev.bitspittle.racketeer.model.game

import dev.bitspittle.racketeer.model.card.*
import dev.bitspittle.racketeer.model.shop.MutableShop
import dev.bitspittle.racketeer.model.shop.Shop
import kotlin.random.Random

//class GameState2 private constructor(
//    turn: Int,
//    cash: Int,
//    influence: Int,
//    luck: Int,
//    vp: Int,
//    handSize: Int,
//    shopTier: Int,
//) {

class GameState(
    private val data: GameData,
) {
    private val random = Random.Default

    /**
     * 0-indexed turn
     */
    var turn = 0
        private set

    /**
     * How much cash the player currently has.
     *
     * Will be cleared at the end of the current turn.
     */
    var cash = 0
        private set

    /**
     * How much influence the player currently has.
     */
    var influence = 0
        private set

    /**
     * How many points of luck the player currently has.
     *
     * Luck can be used to re-roll the shop.
     */
    var luck = 0
        private set

    /**
     * How many victory points the player currently has.
     *
     * Luck can be used to re-roll the shop.
     */
    var vp = 0
        private set

    /**
     * How many cards get drawn at the beginning of the turn.
     */
    var handSize = data.initialHandSize
        private set

    /**
     * The 0-indexed tier value of the shop (i.e. how many times it has been upgraded)
     */
    var shopTier = 0
        private set

    private val _shop = MutableShop()

    private val _deck = MutablePile(data.initialDeck
        .flatMap {  entry ->
            val cardName = entry.substringBeforeLast(' ')
            val count = entry.substringAfterLast(' ', missingDelimiterValue = "").toIntOrNull() ?: 1

            val card = data.cards.single { it.name == cardName }
            List(count) { card.instantiate() }
        }
        .toMutableList()
        .apply { shuffle() }
    )
    private val _hand = MutablePile()
    private val _street = MutablePile()
    private val _discard = MutablePile()
    private val _jail = MutablePile()

    val shop: Shop = _shop

    val deck: Pile = _deck
    val hand: Pile = _hand
    val street: Pile = _street
    val discard: Pile = _discard
    val jail: Pile = _jail

    private val cardPiles = mutableMapOf<Card, MutablePile>()
    init {
        listOf(_deck, _street, _hand, _discard, _jail).forEach { pile ->
            pile.cards.forEach { card -> cardPiles[card] = pile }
        }
    }

    private fun move(card: Card, pile: MutablePile, insertStrategy: InsertStrategy = InsertStrategy.BACK) {
        move(listOf(card), pile, insertStrategy)
    }

    private fun move(cards: List<Card>, pile: MutablePile, insertStrategy: InsertStrategy = InsertStrategy.BACK) {
        cards.forEach { card ->
            cardPiles.remove(card)?.also { pile -> pile.cards.remove(card) }
            cardPiles[card] = pile
        }
        pile.cards.insert(cards, insertStrategy, random)
    }

    private fun move(pileFrom: MutablePile, pileTo: MutablePile, insertStrategy: InsertStrategy = InsertStrategy.BACK) {
        if (pileFrom === pileTo) {
            throw GameException("Attempting to move a pile of cards into itself")
        }

        pileFrom.cards.forEach { card -> cardPiles[card] = pileTo }
        pileTo.cards.insert(pileFrom.cards, insertStrategy, random)
        pileFrom.cards.clear()
    }

    private fun dieIfGameOver() {
        if (turn >= data.numTurns) throw GameException("Can't change game state after the game is already over!")
    }

    fun draw(count: Int = handSize) {
        dieIfGameOver()

        var remainingCount = count.coerceAtMost(deck.cards.size + discard.cards.size)
        if (remainingCount == 0) return

        while (remainingCount > 0 && _deck.cards.isNotEmpty()) {
            move(_deck.cards.removeFirst(), _hand)
            --remainingCount
        }

        if (remainingCount > 0) {
            move(_discard, _deck)
            _deck.cards.shuffle()
        }

        while (remainingCount > 0) {
            move(_deck.cards.removeFirst(), _hand)
            --remainingCount
        }
    }

    fun play(handIndex: Int) {
        dieIfGameOver()

        if (handIndex !in _hand.cards.indices) {
            throw GameException("Attempt to play hand card with invalid index $handIndex")
        }
        // TODO: Run actions
        move(_hand.cards.removeAt(handIndex), _street)
    }

    fun endTurn() {
        dieIfGameOver()

        turn++
        cash = 0

        move(_street, _discard)
        move(_hand, _discard)
    }
}