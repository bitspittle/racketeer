package dev.bitspittle.racketeer.model.game

import com.benasher44.uuid.Uuid
import dev.bitspittle.limp.types.ListStrategy
import dev.bitspittle.racketeer.model.action.Enqueuers
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.MutableCard
import dev.bitspittle.racketeer.model.card.vpTotal
import dev.bitspittle.racketeer.model.pile.MutablePile
import dev.bitspittle.racketeer.model.pile.Pile
import dev.bitspittle.racketeer.model.random.CopyableRandom
import dev.bitspittle.racketeer.model.shop.MutableShop
import dev.bitspittle.racketeer.model.shop.Shop
import kotlin.math.max

interface GameState {
    val random: CopyableRandom
    val numTurns: Int
    val turn: Int
    val cash: Int
    val influence: Int
    val luck: Int
    val vp: Int
    val handSize: Int
    val shop: Shop
    val deck: Pile
    val hand: Pile
    val street: Pile
    val discard: Pile
    val jail: Pile
    val graveyard: Pile
    val effects: Effects
    val history: List<GameStateChange>

    /**
     * Inform this state that something changed and you'd like to recalculate the VP amount.
     */
    // It would be nice if this could be done automatically, but with the current architecture, this has to be done
    // in a suspend context, so it requires a parent suspend function calling it right now. Maybe this can be revisited
    // later.
    suspend fun updateVictoryPoints()
    suspend fun apply(change: GameStateChange)
    fun copy(): GameState

    fun pileFor(card: Card): Pile?
}

val GameState.lastTurnIndex get() = numTurns - 1
val GameState.hasGameStarted get() = !(turn == 0 && history.isEmpty() && getOwnedCards().all { pileFor(it) == deck })
val GameState.isGameOver get() = history.lastOrNull() is GameStateChange.GameOver
val GameState.isGameInProgress get() = hasGameStarted && !isGameOver
val GameState.ownedPiles: Sequence<Pile> get() = sequenceOf(hand, deck, discard, street)
val GameState.allPiles: Sequence<Pile> get() = ownedPiles + sequenceOf(jail, graveyard)
val GameState.allCards: Sequence<Card> get() = allPiles.flatMap { it.cards }
fun GameState.getOwnedCards() = ownedPiles
    .flatMap { it.cards }

class MutableGameState internal constructor(
    override val random: CopyableRandom,
    val enqueuers: Enqueuers,
    numTurns: Int,
    turn: Int,
    cash: Int,
    influence: Int,
    luck: Int,
    vp: Int,
    handSize: Int,
    override val shop: MutableShop,
    override val deck: MutablePile,
    override val hand: MutablePile,
    override val street: MutablePile,
    override val discard: MutablePile,
    override val jail: MutablePile,
    override val graveyard: MutablePile,
    override val effects: MutableEffects,
    override val history: MutableList<GameStateChange>,
): GameState {
    constructor(data: GameData, enqueuers: Enqueuers, random: CopyableRandom = CopyableRandom()) : this(
        random,
        enqueuers,
        numTurns = data.numTurns,
        turn = 0,
        cash = 0,
        influence = data.initialInfluence,
        luck = data.initialLuck,
        vp = 0,
        handSize = data.initialHandSize,
        shop = MutableShop(random, data.cards, data.shopSizes, data.tierFrequencies, data.rarities.map { it.frequency }),
        deck = MutablePile(data.initialDeck
            .flatMap { entry ->
                val cardName = entry.substringBeforeLast(' ')
                val initialCount = entry.substringAfterLast(' ', missingDelimiterValue = "").toIntOrNull() ?: 1

                val card = data.cards.single { it.name == cardName }
                List(initialCount) { card.instantiate() }
            }.shuffled(random())
            .toMutableList(),
        ),
        hand = MutablePile(),
        street = MutablePile(),
        discard = MutablePile(),
        jail = MutablePile(),
        graveyard = MutablePile(),
        effects = MutableEffects(),
        history = mutableListOf()
    )

    /**
     * How many turns are in a game.
     */
    override var numTurns = numTurns
        set(value) {
            field = value.coerceAtLeast(turn + 1)
        }

    /**
     * 0-indexed turn
     */
    override var turn = turn
        set(value) {
            field = value.coerceAtMost(lastTurnIndex)
        }

    /**
     * How much cash the player currently has.
     *
     * Will be cleared at the end of the current turn.
     */
    override var cash = cash
        set(value) {
            field = value.coerceAtLeast(0)
        }

    /**
     * How much influence the player currently has.
     */
    override var influence = influence
        set(value) {
            field = value.coerceAtLeast(0)
        }

    /**
     * How many points of luck the player currently has.
     *
     * Luck can be used to re-roll the shop.
     */
    override var luck = luck
        set(value) {
            field = value.coerceAtLeast(0)
        }

    /**
     * How many victory points the player currently has.
     *
     * See also: [updateVictoryPoints].
     */
    override var vp = vp
        private set

    /**
     * How many cards get drawn at the beginning of the turn.
     */
    override var handSize = handSize
        set(value) {
            field = value.coerceAtLeast(max(1, field))
        }

    private val _allPiles = listOf(hand, deck, discard, street, jail, graveyard)

    private val cardPiles = mutableMapOf<Uuid, MutablePile>()
    init {
        _allPiles.forEach { pile ->
            pile.cards.forEach { card -> cardPiles[card.id] = pile }
        }
    }

    private fun MutableList<MutableCard>.insert(cards: List<MutableCard>, listStrategy: ListStrategy, random: CopyableRandom) {
        when (listStrategy) {
            ListStrategy.FRONT -> cards.forEachIndexed { i, card -> this.add(i, card) }
            ListStrategy.BACK -> cards.forEach { card -> this.add(card) }
            ListStrategy.RANDOM -> cards.forEach { card ->
                val index = random.nextInt(this.size + 1)
                this.add(index, card)
            }
        }
    }

    override fun pileFor(card: Card): Pile? = cardPiles[card.id]

    suspend fun move(card: Card, pileTo: Pile, listStrategy: ListStrategy = ListStrategy.BACK) {
        move(listOf(card), pileTo, listStrategy)
    }

    override suspend fun updateVictoryPoints() {
        val owned = getOwnedCards()
        owned.forEach { card -> enqueuers.card.enqueuePassiveActions(this, card) }
        enqueuers.actionQueue.runEnqueuedActions()

        vp = owned.sumOf { card -> card.vpTotal }
    }

    private fun Card.isOwned() = cardPiles[id].let { pile -> pile != null && pile in ownedPiles }

    @Suppress("NAME_SHADOWING")
    suspend fun move(cards: List<Card>, toPile: Pile, listStrategy: ListStrategy = ListStrategy.BACK) {
        // Any cards that go from being unowned to owned should be initialized; including cards from the jail
        val unownedBeforeMove = cards.filter { !it.isOwned() }

        // Move the cards
        run {
            val pileTo = _allPiles.single { it.id == toPile.id }
            // Make a copy of the list of cards, as modifying the piles below may inadvertently change the list as well,
            // due to some internal, aggressive casting between piles and mutable piles
            val cards = cards.toList()
            cards.forEach { card ->
                remove(card)
                cardPiles[card.id] = pileTo
            }
            pileTo.cards.insert(cards.map { it as MutableCard }, listStrategy, random)
        }

        // ... then execute their actions
        run {
            unownedBeforeMove.filter { it.template.initActions.isNotEmpty() }.forEach { card -> enqueuers.card.enqueueInitActions(this, card) }
            enqueuers.actionQueue.runEnqueuedActions()

            // Trigger effects that are listening for new card effects
            unownedBeforeMove.forEach { card -> effects.processCardCreated(card) }
        }
    }

    private fun remove(card: Card) {
        cardPiles.remove(card.id)?.also { pileFrom -> pileFrom.cards.removeAll { it.id == card.id }}
        shop.remove(card.id)
    }

    override fun copy(): MutableGameState {
        val random = random.copy()
        return MutableGameState(
            random,
            enqueuers,
            numTurns,
            turn,
            cash,
            influence,
            luck,
            vp,
            handSize,
            shop.copy(random),
            deck.copy(),
            hand.copy(),
            street.copy(),
            discard.copy(),
            jail.copy(),
            graveyard.copy(),
            effects.copy(),
            history.toMutableList(),
        )
    }

    override suspend fun apply(change: GameStateChange) = apply(change, null)

    /**
     * @param insertBefore Sometimes it's useful for a change to delegate to another change, which should technically
     *   happen before it does. This results in better reporting the game state to the user, but it should be relatively
     *   rare.
     */
    suspend fun apply(change: GameStateChange, insertBefore: GameStateChange?) {
        if (history.lastOrNull() is GameStateChange.GameOver) return

        // We postpone applying the init delta because when we first construct this game state, we're not in a
        // suspend fun context
        if (!hasGameStarted) {
            history.add(GameStateChange.GameStarted())
        }

        if (insertBefore == null) {
            history.add(change)
        } else {
            history.add(history.indexOf(insertBefore), change)
        }
        change.applyTo(this)
        check(hasGameStarted)
    }
}