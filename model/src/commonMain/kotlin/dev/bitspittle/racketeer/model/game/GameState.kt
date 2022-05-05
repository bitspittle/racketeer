package dev.bitspittle.racketeer.model.game

import com.benasher44.uuid.Uuid
import dev.bitspittle.limp.types.ListStrategy
import dev.bitspittle.racketeer.model.card.*
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
    val streetEffects: List<Effect>
    val history: List<GameStateDelta>

    suspend fun apply(delta: GameStateDelta)
    fun copy(): GameState

    fun pileFor(card: Card): Pile?
}

val GameState.lastTurnIndex get() = numTurns - 1
val GameState.isGameOver get() = history.last() is GameStateDelta.GameOver
val GameState.allPiles: List<Pile> get() = listOf(hand, deck, discard, street, jail)
fun GameState.getOwnedCards() = allPiles.flatMap { it.cards }

class MutableGameState internal constructor(
    override val random: CopyableRandom,
    val cardQueue: CardQueue,
    private val onCardOwned: (CardTemplate) -> Unit,
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
    override val streetEffects: MutableList<Effect>,
    override val history: MutableList<GameStateDelta>
): GameState {
    constructor(data: GameData, cardQueue: CardQueue, random: CopyableRandom, onCardOwned: (CardTemplate) -> Unit) : this(
        random = random,
        cardQueue = cardQueue,
        onCardOwned = onCardOwned,
        numTurns = data.numTurns,
        turn = 0,
        cash = 0,
        influence = data.initialInfluence,
        luck = data.initialLuck,
        vp = 0,
        handSize = data.initialHandSize,
        shop = MutableShop(random, data.cards, data.shopSizes, data.tierFrequencies, data.rarities.map { it.frequency }),
        deck = MutablePile(),
        hand = MutablePile(),
        street = MutablePile(),
        discard = MutablePile(),
        jail = MutablePile(),
        streetEffects = mutableListOf(),
        history = mutableListOf()
    ) {
        // Since we create the game state before the scripting system, it's best not to have initialization / passive
        // logic in cards that are installed from the beginning. If it becomes important to support this later, we can
        // refactor the code to not set up the deck until AFTER the scripting system is up and running.
        require(deck.cards.all { it.template.initActions.isEmpty() }) { "Initial actions on starting cards are not supported" }
        require(deck.cards.all { it.template.passiveActions.isEmpty() }) { "Passive actions on starting cards are not supported" }

        // Will probably be 0 anyway but just in case
        vp = deck.cards.sumOf { it.vpTotal }

        // Run the first time [apply] is called
        history.add(GameStateDelta.Init(data.cards, data.initialDeck))
    }

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

    private val _allPiles = listOf(hand, deck, discard, street, jail)

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

    // Needs to be suspend because it might trigger init actions
    suspend fun move(card: Card, pileTo: Pile, listStrategy: ListStrategy = ListStrategy.BACK) {
        move(listOf(card), pileTo, listStrategy)
    }

    // TODO: Audit where we call this -- we can probably be smarter and call it way less
    suspend fun updateVictoryPoints() {
        val owned = getOwnedCards()
        owned.forEach { cardQueue.enqueuePassiveActions(it) }
        cardQueue.runEnqueuedActions(this@MutableGameState)

        vp = owned.sumOf { card -> card.vpTotal } + jail.cards.filter { card -> card.isJailbird() }.sumOf { card -> card.vpTotal }
    }

    // Needs to be suspend because it might trigger init actions
    @Suppress("NAME_SHADOWING")
    suspend fun move(cards: List<Card>, toPile: Pile, listStrategy: ListStrategy = ListStrategy.BACK) {
        // Any cards that go from being unowned to owned should be initialized; including cards from the jail
        val newlyOwnedCards = cards.filter { card -> !cardPiles.contains(card.id) }
        val cardsToInit = cards
            .filter { card -> card.template.initActions.isNotEmpty() && cardPiles[card.id].let { it == null || it == jail} }

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
            cardsToInit.forEach { cardQueue.enqueueInitActions(it) }
            cardQueue.runEnqueuedActions(this)
            updateVictoryPoints()

            newlyOwnedCards.forEach { onCardOwned(it.template) }
        }
    }

    // Guaranteed owned card to owned card - it's not necessary to worry about running init actions (so no need to be
    // suspend)
    @Suppress("NAME_SHADOWING")
    fun move(pileFrom: Pile, pileTo: Pile, listStrategy: ListStrategy = ListStrategy.BACK) {
        val pileFrom = _allPiles.single { it.id == pileFrom.id }
        val pileTo = _allPiles.single { it.id == pileTo.id }
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

    private fun remove(card: Card) {
        cardPiles.remove(card.id)?.also { pileFrom -> pileFrom.cards.removeAll { it.id == card.id }}
        shop.remove(card.id)
    }

    override fun copy(): MutableGameState {
        val random = random.copy()
        return MutableGameState(
            random,
            cardQueue,
            onCardOwned,
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
            streetEffects.toMutableList(),
            history.toMutableList(),
        )
    }

    override suspend fun apply(delta: GameStateDelta) {
        if (history.last() is GameStateDelta.GameOver) return

        // We postpone applying the init delta because when we first construct this game state, we're not in a
        // suspend fun context
        if (history.size == 1) {
            check(history[0] is GameStateDelta.Init)
            history[0].applyTo(this)
        }

        history.add(delta)
        delta.applyTo(this)

        updateVictoryPoints()
    }
}