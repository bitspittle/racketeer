package dev.bitspittle.racketeer.model.game

import dev.bitspittle.limp.types.ListStrategy
import dev.bitspittle.racketeer.model.card.CardTemplate
import kotlinx.serialization.Serializable

@Serializable
sealed class GameStateDelta {
    abstract suspend fun applyTo(state: MutableGameState)

    @Serializable
    class Init(val allCards: List<CardTemplate>, val initialDeck: List<String>) : GameStateDelta() {
        override suspend fun applyTo(state: MutableGameState) {
            check(state.history.size == 1) { "Error: initializing a game that is already initialized" }
            // Convert "somecard 5" to 5 instances of "somecard"
            state.move(
                initialDeck
                    .flatMap { entry ->
                        val cardName = entry.substringBeforeLast(' ')
                        val initialCount = entry.substringAfterLast(' ', missingDelimiterValue = "").toIntOrNull() ?: 1

                        val card = allCards.single { it.name == cardName }
                        List(initialCount) { card.instantiate() }
                    },
                state.deck,
                listStrategy = ListStrategy.RANDOM
            )
        }
    }

    @Serializable
    class ShuffleDiscardIntoDeck : GameStateDelta() {
        override suspend fun applyTo(state: MutableGameState) {
            state.move(state.discard.cards.shuffled(state.random()), state.deck)
        }
    }

    @Serializable
    class Draw(val count: Int) : GameStateDelta() {
        override suspend fun applyTo(state: MutableGameState) {
            val remainingCount = count.coerceAtMost(state.deck.cards.size)
            if (remainingCount == 0) return

            state.deck.cards.take(remainingCount).let { cards ->
                check(cards.size == remainingCount) // Should be guaranteed by our coerce line at the top
                state.move(cards, state.hand)
            }
        }
    }
}