package dev.bitspittle.racketeer.model.game

import dev.bitspittle.limp.types.ListStrategy
import dev.bitspittle.racketeer.model.card.*
import dev.bitspittle.racketeer.model.pile.MutablePile
import dev.bitspittle.racketeer.model.pile.Pile
import dev.bitspittle.racketeer.model.shop.Exclusion

@Suppress("CanSealedSubClassBeObject") // All subclasses not objects, for consistency / future proofing
sealed class GameStateChange {
    suspend fun applyTo(state: MutableGameState) = state.apply()
    protected abstract suspend fun MutableGameState.apply()

    class GameStarted : GameStateChange() {
        // Do nothing, this is just a marker game state
        override suspend fun MutableGameState.apply() = Unit
    }

    class ShuffleDiscardIntoDeck : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            move(discard.cards.shuffled(random()), deck)
        }
    }

    class Draw(var count: Int) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            if (deck.cards.size < count && discard.cards.isNotEmpty()) {
                apply(ShuffleDiscardIntoDeck(), insertBefore = this@Draw)
            }

            count = count.coerceAtMost(deck.cards.size)
            deck.cards.take(count).let { cards ->
                move(cards, hand)
            }
        }
    }

    class Play(val handIndex: Int) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            require(handIndex in hand.cards.indices) { "Attempt to play card with an invalid hand index $handIndex, when hand is size ${hand.cards.size}" }
            val card = hand.cards[handIndex]

            apply(MoveCard(card, street))

            // Apply upgrades *first*, as otherwise, playing a card may add an upgrade which shouldn't take affect until
            // a later turn.
            if (card.isDexterous) apply(AddGameAmount(GameProperty.CASH, 1))
            if (card.isArtful) apply(AddGameAmount(GameProperty.INFLUENCE, 1))
            if (card.isLucky) apply(AddGameAmount(GameProperty.LUCK, 1))

            // Playing this card might install an effect, but that shouldn't take effect until the next card is played
            val streetEffectsCopy = streetEffects.toList()
            cardQueue.enqueuePlayActions(card)
            cardQueue.runEnqueuedActions(this)
            streetEffectsCopy.forEach { streetEffect -> streetEffect.invoke(card) }

            if (card.isVeteran) apply(Draw(1))
        }
    }

    class MoveCard(val card: Card, val intoPile: Pile, val listStrategy: ListStrategy = ListStrategy.BACK) :
        GameStateChange() {
        override suspend fun MutableGameState.apply() {
            move(card, intoPile, listStrategy)
        }
    }

    class MoveCards(val cards: List<Card>, val intoPile: Pile, val listStrategy: ListStrategy = ListStrategy.BACK) :
        GameStateChange() {
        override suspend fun MutableGameState.apply() {
            move(cards, intoPile, listStrategy)
        }
    }

    class Shuffle(val pile: Pile) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            (pile as MutablePile).cards.shuffle(random())
        }
    }

    class AddCardAmount(val property: CardProperty, val card: Card, val amount: Int) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            when (property) {
                CardProperty.COUNTER -> (card as MutableCard).counter += amount
                CardProperty.VP -> (card as MutableCard).vpBase += amount
                CardProperty.VP_PASSIVE -> (card as MutableCard).vpPassive += amount
                else -> error("Can't change read-only card property $property")
            }
        }
    }

    class UpgradeCard(val card: Card, val upgradeType: UpgradeType) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            (card as MutableCard).upgrades.add(upgradeType)
        }
    }

    class AddGameAmount(val property: GameProperty, val amount: Int) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            when (property) {
                GameProperty.CASH -> cash += amount
                GameProperty.INFLUENCE -> influence += amount
                GameProperty.LUCK -> luck += amount
                GameProperty.HAND_SIZE -> handSize += amount
                else -> error("Can't change read-only game property $property")
            }
        }
    }

    class AddStreetEffect(val effect: Effect) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            streetEffects.add(effect)
        }
    }

    class AddShopExclusion(val exclusion: Exclusion) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            shop.addExclusion(exclusion)
        }
    }

    class RestockShop(private val additionalFilter: suspend (CardTemplate) -> Boolean = { true }) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            shop.restock(additionalFilter = additionalFilter)
        }
    }

    class UpgradeShop() : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            shop.upgrade()
        }
    }

    class EndTurn : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            if (turn == lastTurnIndex) {
                apply(GameOver())
                return
            }

            turn++
            cash = 0

            streetEffects.clear()
            move(street.cards, discard)
            move(hand.cards, discard)
            shop.restock()
        }
    }

    class GameOver : GameStateChange() {
        // Doesn't do anything; just a marker that this game is finished
        override suspend fun MutableGameState.apply() = Unit
    }
}