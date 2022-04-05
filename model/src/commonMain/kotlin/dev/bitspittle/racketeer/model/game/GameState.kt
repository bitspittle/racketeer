package dev.bitspittle.racketeer.model.game

import dev.bitspittle.limp.types.ListStrategy
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
//    val shop: Shop = _shop
//    val deck: Pile = _deck
//    val hand: Pile = _hand
//    val street: Pile = _street
//    val discard: Pile = _discard
//    val jail: Pile = _jail
//) {

class GameState internal constructor(
    turn: Int,
    cash: Int,
    influence: Int,
    luck: Int,
    vp: Int,
    handSize: Int,
    shopTier: Int,
    shop: MutableShop,
    deck: MutablePile,
    hand: MutablePile,
    street: MutablePile,
    discard: MutablePile,
    jail: MutablePile,
    private val random: Random,
) {
    constructor(data: GameData, random: Random = Random.Default) : this(
        turn = 0,
        cash = 0,
        influence = 0,
        luck = 0,
        vp = 0,
        handSize = data.initialHandSize,
        shopTier = 0,
        shop = MutableShop(),
        deck = MutablePile(data.initialDeck
            .flatMap {  entry ->
                val cardName = entry.substringBeforeLast(' ')
                val count = entry.substringAfterLast(' ', missingDelimiterValue = "").toIntOrNull() ?: 1

                val card = data.cards.single { it.name == cardName }
                List(count) { card.instantiate() }
            }
            .toMutableList()
            .apply { shuffle() }
        ),
        hand = MutablePile(),
        street = MutablePile(),
        discard = MutablePile(),
        jail = MutablePile(),
        random = random
    )

    /**
     * 0-indexed turn
     */
    var turn = turn
        private set

    /**
     * How much cash the player currently has.
     *
     * Will be cleared at the end of the current turn.
     */
    var cash = cash
        set(value) {
            field = value.coerceAtLeast(0)
        }

    /**
     * How much influence the player currently has.
     */
    var influence = influence
        set(value) {
            field = value.coerceAtLeast(0)
        }

    /**
     * How many points of luck the player currently has.
     *
     * Luck can be used to re-roll the shop.
     */
    var luck = luck
        set(value) {
            field = value.coerceAtLeast(0)
        }

    /**
     * How many victory points the player currently has.
     *
     * Luck can be used to re-roll the shop.
     */
    var vp = vp
        set(value) {
            field = value.coerceAtLeast(0)
        }

    /**
     * How many cards get drawn at the beginning of the turn.
     */
    var handSize = handSize
        set(value) {
            field = value.coerceAtLeast(1)
        }

    /**
     * The 0-indexed tier value of the shop (i.e. how many times it has been upgraded)
     */
    var shopTier = shopTier
        private set

    private val _shop = shop

    private val _deck = deck
    private val _hand = hand
    private val _street = street
    private val _discard = discard
    private val _jail = jail

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

    fun move(card: Card, pileTo: Pile, listStrategy: ListStrategy = ListStrategy.BACK) {
        move(listOf(card), pileTo, listStrategy)
    }

    fun move(cards: List<Card>, pileTo: Pile, listStrategy: ListStrategy = ListStrategy.BACK) {
        val pileTo = pileTo as MutablePile
        cards.forEach { card ->
            cardPiles.remove(card)?.also { pile -> pile.cards.remove(card) }
            cardPiles[card] = pileTo
        }
        pileTo.cards.insert(cards, listStrategy, random)
    }

    fun move(pileFrom: Pile, pileTo: Pile, listStrategy: ListStrategy = ListStrategy.BACK) {
        val pileFrom = pileFrom as MutablePile
        val pileTo = pileTo as MutablePile
        if (pileFrom === pileTo) {
            throw GameException("Attempting to move a pile of cards into itself")
        }

        pileFrom.cards.forEach { card -> cardPiles[card] = pileTo }
        pileTo.cards.insert(pileFrom.cards, listStrategy, random)
        pileFrom.cards.clear()
    }

    fun draw(count: Int = handSize) {
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

    fun endTurn() {
        turn++
        cash = 0

        move(_street, _discard)
        move(_hand, _discard)
    }

    fun copy(): GameState {
        return GameState(
            turn,
            cash,
            influence,
            luck,
            vp,
            handSize,
            shopTier,
            _shop.copy(),
            _deck.copy(),
            _hand.copy(),
            _street.copy(),
            _discard.copy(),
            _jail.copy(),
            random
        )
    }
}