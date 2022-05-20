package dev.bitspittle.racketeer.model.game

import dev.bitspittle.limp.types.ListStrategy
import dev.bitspittle.racketeer.model.building.*
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
            effects.processPileShuffed(deck)
        }
    }

    class Draw(var count: Int) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            val isFirstDrawThisTurn = run {
                check(history.last() === this@Draw)
                val prevChange = history.dropLast(1).lastOrNull { it is Draw || it is EndTurn }
                prevChange !is Draw
            }

            if (deck.cards.size < count && discard.cards.isNotEmpty()) {
                apply(ShuffleDiscardIntoDeck(), insertBefore = this@Draw)
            }

            count = count.coerceAtMost(deck.cards.size)
            deck.cards.take(count).let { cards ->
                move(cards, hand)
            }

            if (isFirstDrawThisTurn) {
                effects.processTurnStarted()
            }
        }
    }

    class Play(val handIndex: Int) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            require(handIndex in hand.cards.indices) { "Attempt to play card with an invalid hand index $handIndex, when hand is size ${hand.cards.size}" }
            val card = hand.cards[handIndex]

            apply(MoveCard(card, street))

            // Apply upgrades *first*, as otherwise, playing a card may add an upgrade which shouldn't take effect until
            // a later turn.
            if (card.isDexterous) apply(AddGameAmount(GameProperty.CASH, 1))
            if (card.isArtful) apply(AddGameAmount(GameProperty.INFLUENCE, 1))
            if (card.isLucky) apply(AddGameAmount(GameProperty.LUCK, 1))
            if (card.isVeteran) apply(Draw(1))

            // Trigger effects first. Once we play a card, it might install an additional effect, which we don't want
            // to immediately run against the card itself
            effects.processCardPlayed(card)
            enqueuers.card.enqueuePlayActions(this, card)
            enqueuers.actionQueue.runEnqueuedActions()
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
            effects.processPileShuffed(pile)
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
        init {
            check(!upgradeType.isInternal()) { "Unexpected request to upgrade card with internal upgrade type: $upgradeType"}
        }
        override suspend fun MutableGameState.apply() {
            (card as MutableCard).upgrades.add(upgradeType)
        }
    }

    class AddBuildingAmount(val property: BuildingProperty, val building: Building, val amount: Int) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            when (property) {
                BuildingProperty.COUNTER -> (building as MutableBuilding).counter += amount
                BuildingProperty.VP -> (building as MutableBuilding).vpBase += amount
                BuildingProperty.VP_PASSIVE -> (building as MutableBuilding).vpPassive += amount
                else -> error("Can't change read-only building property $property")
            }
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

    class AddEffect(val effect: Effect<*>) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            effects.items.add(effect)
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

    class UpgradeShop : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            shop.upgrade()
        }
    }

    class Build(val blueprintIndex: Int) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            require(blueprintIndex in blueprints.indices) { "Attempt to build a blueprint with an invalid index $blueprintIndex, when blueprint count is ${blueprints.size}" }
            val blueprint = blueprints[blueprintIndex]

            blueprints.remove(blueprint)
            val building = blueprint.build()
            buildings.add(building).also { buildings.sort() }

            enqueuers.building.enqueueInitActions(this, building)
            enqueuers.actionQueue.runEnqueuedActions()
        }
    }

   class Activate(val buildingIndex: Int) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            require(buildingIndex in buildings.indices) { "Attempt to activate a building with an invalid index $buildingIndex, when building count is ${buildings.size}" }
            val building = buildings[buildingIndex]
            require(!building.isActivated) { "Attempt to activate a building that was already activated" }
            building.isActivated = true

            // Run its activate actions.
            enqueuers.building.enqueueActivateActions(this, building)
            enqueuers.actionQueue.runEnqueuedActions()
        }
    }

    class AddBlueprint(val blueprint: Blueprint) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            require(!blueprint.isOwned(this)) { "Attempt to add blueprint \"${blueprint.name}\" that was already owned" }
            blueprints.add(blueprint).also { blueprints.sort() }
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

            effects.processTurnEnded()
            move(street.cards, discard)
            move(hand.cards, discard)
            shop.restock()

            buildings.forEach { it.isActivated = false }
        }
    }

    class GameOver : GameStateChange() {
        // Doesn't do anything; just a marker that this game is finished
        override suspend fun MutableGameState.apply() = Unit
    }
}