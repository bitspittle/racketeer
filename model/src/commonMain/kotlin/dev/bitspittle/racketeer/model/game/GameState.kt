package dev.bitspittle.racketeer.model.game

import com.benasher44.uuid.Uuid
import dev.bitspittle.limp.types.ListStrategy
import dev.bitspittle.racketeer.model.card.*
import dev.bitspittle.racketeer.model.shop.MutableShop
import dev.bitspittle.racketeer.model.shop.Shop
import kotlin.random.Random

class GameState internal constructor(
    private val random: Random,
    val allCards: List<CardTemplate>,
    private val cardQueue: CardQueue,
    numTurns: Int,
    turn: Int,
    totalCashEarned: Int,
    cash: Int,
    influence: Int,
    luck: Int,
    vp: Int,
    handSize: Int,
    shop: MutableShop,
    deck: MutablePile,
    hand: MutablePile,
    street: MutablePile,
    discard: MutablePile,
    jail: MutablePile,
    streetEffects: MutableList<Effect>,
) {
    constructor(data: GameData, cardQueue: CardQueue, random: Random = Random.Default) : this(
        random = random,
        allCards = data.cards,
        cardQueue = cardQueue,
        numTurns = data.numTurns,
        turn = 0,
        totalCashEarned = 0,
        cash = 0,
        influence = 0,
        luck = data.initialLuck,
        vp = 0,
        handSize = data.initialHandSize,
        shop = MutableShop(random, data.cards, data.shopSizes, data.tiers.map { it.frequency }),
        deck = MutablePile(data.initialDeck
            .flatMap {  entry ->
                val cardName = entry.substringBeforeLast(' ')
                val count = entry.substringAfterLast(' ', missingDelimiterValue = "").toIntOrNull() ?: 1

                val card = data.cards.single { it.name == cardName }
                List(count) { card.instantiate() }
            }
            .toMutableList()
            .apply { shuffle(random) }
        ),
        hand = MutablePile(),
        street = MutablePile(),
        discard = MutablePile(),
        jail = MutablePile(),
        streetEffects = mutableListOf()
    ) {
        require(deck.cards.all { it.template.initActions.isEmpty() }) { "Initial actions on starting cards are not supported" }
//        require(deck.cards.all { it.template.passiveActions.isEmpty() }) { "Passive actions on starting cards are not supported" }
    }

    /**
     * How many turns are in a game.
     */
    var numTurns = numTurns
        set(value) {
            field = value.coerceAtLeast(turn + 1)
        }

    /**
     * 0-indexed turn
     */
    var turn = turn
        private set

    /**
     * How much cash the player got over the course of the whole game.
     *
     * Not particularly useful when playing, but could be a nice stat to show players on some sort of summary page.
     */
    var totalCashEarned = totalCashEarned
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
        private set

    /**
     * How many cards get drawn at the beginning of the turn.
     */
    var handSize = handSize
        set(value) {
            field = value.coerceAtLeast(1)
        }

    /**
     * A list of 0 more effects that will be applied to each card that is played in the street this turn.
     */
    private val _streetEffects = streetEffects
    val streetEffects get() = _streetEffects.map { it.desc }

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

    fun getOwnedCards() = listOf(deck, hand, street, discard).flatMap { it.cards }

    private val cardPiles = mutableMapOf<Uuid, MutablePile>()
    init {
        listOf(_deck, _street, _hand, _discard, _jail).forEach { pile ->
            pile.cards.forEach { card -> cardPiles[card.id] = pile }
        }
    }

    private fun moveNow(card: Card, toPile: Pile, listStrategy: ListStrategy = ListStrategy.BACK) {
        moveNow(listOf(card), toPile, listStrategy)
    }

    // Call move without triggering card initialization, which means it doesn't need to be suspend
    private fun moveNow(cards: List<Card>, toPile: Pile, listStrategy: ListStrategy = ListStrategy.BACK) {
        val pileTo = toPile as MutablePile
        // Make a copy of the list of cards, as modifying the files below may inadvertently change the list as well,
        // due to some internal, aggressive casting between piles and mutable piles
        val cards = cards.toList()
        cards.forEach { card ->
            remove(card)
            cardPiles[card.id] = toPile
        }
        pileTo.cards.insert(cards, listStrategy, random)
    }

    // Needs to be suspend because it might trigger init actions
    suspend fun move(card: Card, pileTo: Pile, listStrategy: ListStrategy = ListStrategy.BACK) {
        move(listOf(card), pileTo, listStrategy)
    }

    private suspend fun updateVictoryPoints() {
        val owned = getOwnedCards()
        owned.forEach { cardQueue.enqueuePassiveActions(it) }
        cardQueue.runEnqueuedActions(this@GameState)

        vp = owned.sumOf { card -> card.vpTotal }
    }

    // Needs to be suspend because it might trigger init actions
    suspend fun move(cards: List<Card>, toPile: Pile, listStrategy: ListStrategy = ListStrategy.BACK) {
        val cardsToInit = cards
            .filter { card -> card.template.initActions.isNotEmpty() && !cardPiles.contains(card.id) }
        moveNow(cards, toPile, listStrategy)

        cardsToInit.forEach { cardQueue.enqueueInitActions(it) }
        cardQueue.runEnqueuedActions(this)
        updateVictoryPoints()
    }

    // Guaranteed owned card to owned card - it's not necessary to worry about running init actions (so no need to be
    // suspend)
    fun move(pileFrom: Pile, pileTo: Pile, listStrategy: ListStrategy = ListStrategy.BACK) {
        val pileFrom = pileFrom as MutablePile
        val pileTo = pileTo as MutablePile
        if (pileFrom === pileTo) {
            throw GameException("Attempting to move a pile of cards into itself")
        }

        pileFrom.cards.forEach { card -> cardPiles[card.id] = pileTo }
        pileTo.cards.insert(pileFrom.cards, listStrategy, random)
        pileFrom.cards.clear()
    }

    fun remove(cards: List<Card>) {
        // Make a copy of the list of cards, as modifying the files below may inadvertently change the list as well,
        // due to some internal, aggressive casting between piles and mutable piles
        cards.toList().forEach(::remove)
    }

    fun installStreetEffect(desc: String, effect: suspend (Card) -> Unit) {
        _streetEffects.add(Effect(desc, effect))
    }

    private fun remove(card: Card) {
        cardPiles.remove(card.id)?.also { pileFrom -> pileFrom.cards.removeAll { it.id == card.id }}
        _shop.remove(card.id)
    }

    fun draw(count: Int = handSize) {
        var remainingCount = count.coerceAtMost(deck.cards.size + discard.cards.size)
        if (remainingCount == 0) return

        _deck.cards.take(remainingCount).let { cards ->
            remainingCount -= cards.size
            moveNow(cards, _hand)
        }

        if (remainingCount > 0) {
            move(_discard, _deck, ListStrategy.RANDOM)
        }

        _deck.cards.take(remainingCount).let { cards ->
            check(cards.size == remainingCount) // Should be guaranteed by our coerce line at the top
            moveNow(cards, _hand)
        }
    }

    suspend fun play(handIndex: Int) {
        require(handIndex in hand.cards.indices) { "Attempt to play card with an invalid hand index $handIndex, when hand is size ${hand.cards.size}"}
        val card = hand.cards[handIndex]

        moveNow(card, street)

        // Playing this card might install an effect, but that shouldn't take effect until the next card is played
        val streetEffectsCopy = _streetEffects.toList()
        cardQueue.enqueuePlayActions(card)
        cardQueue.runEnqueuedActions(this)
        streetEffectsCopy.forEach { streetEffect -> streetEffect.invoke(card) }
        updateVictoryPoints()
    }

    fun endTurn(): Boolean {
        // Always remove cash, even if there are no more turns. This way, the final reporting page summarizing your
        // won't show weird leftover cash.
        totalCashEarned += cash
        cash = 0

        if (turn >= numTurns - 1) return false

        turn++

        _streetEffects.clear()
        move(_street, _discard)
        moveNow(_hand.cards.filter { !it.isPatient() }, _discard)
        shop.restockNow()

        return true
    }

    fun copy(): GameState {
        return GameState(
            random,
            allCards,
            cardQueue,
            numTurns,
            turn,
            totalCashEarned,
            cash,
            influence,
            luck,
            vp,
            handSize,
            _shop.copy(),
            _deck.copy(),
            _hand.copy(),
            _street.copy(),
            _discard.copy(),
            _jail.copy(),
            _streetEffects.toMutableList()
        )
    }
}