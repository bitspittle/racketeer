package dev.bitspittle.racketeer.model.game

import dev.bitspittle.limp.types.ListStrategy
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.model.card.isUndercover
import dev.bitspittle.racketeer.model.pile.MutablePile

sealed class GameStateDelta {
    suspend fun applyTo(state: MutableGameState) = state.apply()
    protected abstract suspend fun MutableGameState.apply()

    class Init(val allCards: List<CardTemplate>, val initialDeck: List<String>) : GameStateDelta() {
        override suspend fun MutableGameState.apply() {
            check(history.size == 1 && deck.cards.isEmpty()) { "Error: initializing a game that is already initialized" }
            // Convert "somecard 5" to 5 instances of "somecard"
            move(
                initialDeck
                    .flatMap { entry ->
                        val cardName = entry.substringBeforeLast(' ')
                        val initialCount = entry.substringAfterLast(' ', missingDelimiterValue = "").toIntOrNull() ?: 1

                        val card = allCards.single { it.name == cardName }
                        List(initialCount) { card.instantiate() }
                    },
                deck,
                listStrategy = ListStrategy.RANDOM
            )
        }
    }

    class ShuffleDiscardIntoDeck : GameStateDelta() {
        override suspend fun MutableGameState.apply() {
            move(discard.cards.shuffled(random()), deck)
        }
    }

    class Draw(val count: Int) : GameStateDelta() {
        override suspend fun MutableGameState.apply() {
            if (deck.cards.size < count && discard.cards.isNotEmpty()) {
                apply(ShuffleDiscardIntoDeck())
            }

            val remainingCount = count.coerceAtMost(deck.cards.size)
            if (remainingCount == 0) return

            deck.cards.take(remainingCount).let { cards ->
                apply(MoveCards(cards, hand))
            }
        }
    }

    class MoveCards(val cards: List<Card>, val intoPile: MutablePile) : GameStateDelta() {
        override suspend fun MutableGameState.apply() {
            move(cards, intoPile)
        }
    }

    class EndTurn : GameStateDelta() {
        override suspend fun MutableGameState.apply() {
            if (turn == lastTurnIndex) {
                apply(GameOver())
                return
            }

            turn++
            cash = 0

            streetEffects.clear()
            move(street, discard)
            move(hand.cards.filter { !it.isUndercover() }, discard)
            shop.restock()
        }
    }

    class GameOver : GameStateDelta() {
        // Doesn't do anything; just a marker that this game is finished
        override suspend fun MutableGameState.apply() = Unit
    }
}