package dev.bitspittle.racketeer.model.game

import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.pile.Pile
import dev.bitspittle.racketeer.model.serialization.DataValue

interface Effects {
    val items: List<Effect<*>>
}

@Suppress("UNCHECKED_CAST") // Casting based on effect type
class MutableEffects private constructor(
    override val items: MutableList<Effect<*>>
) : Effects {
    constructor() : this(mutableListOf())

    /** Process all effects and, when finished, remove any from the list that should be discarded. */
    private suspend fun <T : Any> processForEvent(event: GameEvent, arg: T) {
        val executedOnceEffects = items
            .filter { it.event == event }
            .filter { (it as Effect<T>).invokeConditionally(arg) }
            .filter { it.lifetime == Lifetime.ONCE }

        items.removeAll(executedOnceEffects)

    }

    suspend fun processCardPlayed(card: Card) = processForEvent(GameEvent.PLAY, card)
    suspend fun processCardCreated(card: Card) = processForEvent(GameEvent.CREATE, card)
    suspend fun processPileShuffed(pile: Pile) = processForEvent(GameEvent.SHUFFLE, pile)
    suspend fun processTurnStarted() = processForEvent(GameEvent.TURN_START, Unit)
    suspend fun processTurnEnded() {
        processForEvent(GameEvent.TURN_END, Unit)
        items.removeAll { it.lifetime in listOf(Lifetime.TURN, Lifetime.ONCE) }
    }

    fun copy() = MutableEffects(items.toMutableList())
}

class Effect<T : Any>(
    val desc: String?,
    val lifetime: Lifetime,
    val event: GameEvent,
    val data: DataValue?,
    val testExpr: String?,
    val expr: String,
    private val test: suspend (T) -> Boolean,
    private val action: suspend (T) -> Unit
) {
    suspend fun invokeConditionally(arg: T): Boolean {
        return if (test(arg)) {
            action(arg)
            true
        } else false
    }
}

/**
 * [Effect.expr] prefixed by a warning symbol.
 *
 * Useful indicator to our designers that something internal is leaking to public users if not fixed. */
val Effect<*>.warningExpr get() = "⚠️ $expr"