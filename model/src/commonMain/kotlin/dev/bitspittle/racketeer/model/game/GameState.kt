package dev.bitspittle.racketeer.model.game

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import dev.bitspittle.limp.types.ListStrategy
import dev.bitspittle.racketeer.model.action.Enqueuers
import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.building.Building
import dev.bitspittle.racketeer.model.building.MutableBuilding
import dev.bitspittle.racketeer.model.building.vpTotal
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.MutableCard
import dev.bitspittle.racketeer.model.card.allInitActions
import dev.bitspittle.racketeer.model.card.vpTotal
import dev.bitspittle.racketeer.model.effect.*
import dev.bitspittle.racketeer.model.pile.MutablePile
import dev.bitspittle.racketeer.model.pile.Pile
import dev.bitspittle.racketeer.model.random.CopyableRandom
import dev.bitspittle.racketeer.model.serialization.DataValue
import dev.bitspittle.racketeer.model.shop.MutableShop
import dev.bitspittle.racketeer.model.shop.Shop
import kotlin.math.max

interface GameState {
    val id: Uuid
    val random: CopyableRandom
    val features: Set<Feature.Type>
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
    val blueprints: List<Blueprint>
    val buildings: List<Building>
    val effects: Effects
    val tweaks: Tweaks<Tweak.Game>
    val data: Map<String, DataValue>
    val history: List<GameStateChanges>

    fun pileFor(card: Card): Pile?

    fun canActivate(building: Building): Boolean
}

suspend fun MutableGameState.recordChanges(block: suspend () -> Unit): Boolean {
    startRecordingChanges()
    block()
    return finishRecordingChanges()
}

val GameState.lastTurnIndex get() = numTurns - 1
// Note: Usually "GameOver" is the final marker but occasionally passive VP calculations get applied later. Anyway, as
// long as there's a "GameOver" somewhere in the last group, the game is done.
val GameState.hasGameStarted get() = history.firstOrNull()?.items?.first()?.let { it is GameStateChange.GameStart } ?: false
val GameState.isGameOver get() = history.lastOrNull()?.items?.any { it is GameStateChange.GameOver } ?: false
val GameState.isGameInProgress get() = hasGameStarted && !isGameOver
val GameState.ownedPiles: Sequence<Pile> get() = sequenceOf(hand, deck, discard, street)
val GameState.allPiles: Sequence<Pile> get() = ownedPiles + sequenceOf(jail, graveyard)
val GameState.allCards: Sequence<Card> get() = allPiles.flatMap { it.cards }
fun GameState.getOwnedCards() = ownedPiles
    .flatMap { it.cards }

class MutableGameState internal constructor(
    override val id: Uuid,
    override val random: CopyableRandom,
    override val features: Set<Feature.Type>,
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
    override val blueprints: MutableList<Blueprint>,
    override val buildings: MutableList<MutableBuilding>,
    override val effects: MutableEffects,
    override val tweaks: MutableTweaks<Tweak.Game>,
    override val data: MutableMap<String, DataValue>,
    override val history: MutableList<GameStateChanges>,
): GameState {
    constructor(data: GameData, features: Set<Feature.Type>, enqueuers: Enqueuers, random: CopyableRandom = CopyableRandom()) : this(
        uuid4(),
        random,
        features,
        enqueuers,
        numTurns = data.numTurns,
        turn = 0,
        cash = 0,
        influence = data.initialInfluence,
        luck = data.initialLuck,
        vp = 0,
        handSize = data.initialHandSize,
        shop = MutableShop(random, data.cards, features, data.shopSizes, data.tierFrequencies, data.rarities),
        deck = MutablePile(),
        hand = MutablePile(),
        street = MutablePile(),
        discard = MutablePile(),
        jail = MutablePile(),
        graveyard = MutablePile(),
        blueprints = mutableListOf(),
        buildings = mutableListOf(),
        effects = MutableEffects(),
        tweaks = MutableTweaks(),
        data = mutableMapOf(),
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
     * See also: [onBoardChanged].
     */
    override var vp = vp
        private set(value) {
            field = value.coerceAtLeast(0)
        }

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

    fun findCard(id: Uuid): MutableCard {
        return shop.stock.firstOrNull { card -> card != null && card.id == id }
            ?: _allPiles.asSequence().flatMap { pile -> pile.cards.asSequence() }.first { card -> card.id == id }
    }
    override fun pileFor(card: Card): Pile? = cardPiles[card.id]

    private val _canActivate = mutableMapOf<Uuid, Boolean>()
    override fun canActivate(building: Building): Boolean {
        // We can sometimes activate a building which causes a choice that can end up with us triggering this method
        // without any buildings (because the choice causes an extra refresh, while the building has already been
        // removed because it was activated). Just return false in that case.
        return _canActivate[building.id] ?: false
    }

    suspend fun move(card: Card, pileTo: Pile, listStrategy: ListStrategy = ListStrategy.BACK) {
        move(listOf(card), pileTo, listStrategy)
    }

    /**
     * Inform this state that something changed and you'd like to recalculate any values that may change based on it,
     * e.g. VPs and building states.
     */
    // It would be nice if this could be done automatically, but with the current architecture, this has to be done
    // in a suspend context, so it requires a parent suspend function calling it right now. Maybe this can be revisited
    // later.
    suspend fun onBoardChanged() {
        val owned = getOwnedCards()
        owned.forEach { card -> enqueuers.card.enqueuePassiveActions(this, card) }
        shop.stock.filterNotNull().forEach { card -> enqueuers.card.enqueuePassiveActions(this, card) }
        buildings.forEach { building -> enqueuers.building.enqueuePassiveActions(this, building) }
        enqueuers.actionQueue.runEnqueuedActions()

        vp = owned.sumOf { card -> card.vpTotal } + buildings.sumOf { building -> building.vpTotal }
        buildings.forEach { building ->
            _canActivate[building.id] = enqueuers.building.canActivate(this, building)
        }
    }

    @Suppress("NAME_SHADOWING")
    suspend fun move(cards: List<Card>, toPile: Pile, listStrategy: ListStrategy = ListStrategy.BACK) {
        // Any cards that go from being unowned to owned should be initialized; including cards from the jail
        val unownedBeforeMove = cards.filter { !it.isOwned(this) }

        // Move the cards
        run {
            val pileTo = _allPiles.single { it.id == toPile.id }
            val cards = cards.map { it.toMutableCard(this) }
            cards.forEach { card -> remove(card) }
            pileTo.cards.insert(cards, listStrategy, random)
            cards.forEach { card -> cardPiles[card.id] = pileTo }
        }

        // ... then execute their actions
        run {
            // Note that we don't want to run init actions for cards that move from jail to jail or from the shop into
            // jail. We'll run those init actions if / when the card finally comes out of jail
            val unownedAfterMove = cards.filter { !it.isOwned(this) }.toSet()
            unownedBeforeMove
                .filter { !unownedAfterMove.contains(it) }
                .filter { it.template.allInitActions.isNotEmpty() }
                .forEach { card -> enqueuers.card.enqueueInitActions(this, card) }
            enqueuers.actionQueue.runEnqueuedActions()

            // Trigger effects that are listening for new card effects
            unownedBeforeMove.forEach { card -> effects.processCardCreated(card) }
        }
    }

    private fun remove(card: Card) {
        cardPiles.remove(card.id)?.also { pileFrom ->
            pileFrom.cards.removeAll { it.id == card.id }
        }
        shop.notifyOwned(card.id)
    }

    fun copy(): MutableGameState {
        val random = random.copy()
        return MutableGameState(
            id,
            random,
            features,
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
            blueprints.toMutableList(),
            buildings.map { it.copy() }.toMutableList(),
            effects.copy(),
            tweaks.copy(),
            data.toMutableMap(),
            history.toMutableList(),
        )
    }

    val allChanges: Sequence<GameStateChange>
        get() {
            val prevChanges = history.asSequence().flatMap { it.items.asSequence() }
            val inProgressChanges = (recordingChanges?.items?.asSequence() ?: emptySequence())

            return prevChanges + inProgressChanges
        }

    private var recordingChanges: GameStateChanges? = null

    /** Opens a new change group in [history], which must be done before [addChange] is called. */
    fun startRecordingChanges() {
        recordingChanges = GameStateChanges()
    }

    /** Closed a change group opened by [startRecordingChanges], returning false if no changes were applied. */
    fun finishRecordingChanges(): Boolean {
        if ((recordingChanges?.items?.size ?: 0) == 0) {
            recordingChanges = null
            return false
        }

        val changes = recordingChanges!!
        changes.snapshotResources(this)
        history.add(changes)
        recordingChanges = null
        return true
    }

    suspend fun addChange(change: GameStateChange) {
        if (isGameOver) return

        val changes = recordingChanges ?: error("addChange() called before calling startRecordingChanges()")
        changes.add(change)
        change.applyTo(this)
    }
}

fun Card.isOwned(state: GameState) = state.pileFor(this).let { pile -> pile != null && pile in state.ownedPiles }

fun Card.toMutableCard(state: MutableGameState): MutableCard {
    return this as? MutableCard ?: state.findCard(this.id)
}

fun Pile.toMutablePile(state: MutableGameState): MutablePile {
    return this as? MutablePile ?: state.allPiles.first { pile -> pile.id == id } as MutablePile
}

fun Building.toMutableBuilding(state: MutableGameState): MutableBuilding {
    return this as? MutableBuilding ?: state.buildings.first { bldg -> bldg.id == id }
}