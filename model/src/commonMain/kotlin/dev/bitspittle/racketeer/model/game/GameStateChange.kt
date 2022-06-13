package dev.bitspittle.racketeer.model.game

import dev.bitspittle.limp.types.ListStrategy
import dev.bitspittle.racketeer.model.action.enqueue
import dev.bitspittle.racketeer.model.building.*
import dev.bitspittle.racketeer.model.card.*
import dev.bitspittle.racketeer.model.effect.*
import dev.bitspittle.racketeer.model.pile.MutablePile
import dev.bitspittle.racketeer.model.pile.Pile
import dev.bitspittle.racketeer.model.serialization.DataValue
import dev.bitspittle.racketeer.model.shop.priceFor
import dev.bitspittle.racketeer.model.shop.remaining

private suspend fun MutableGameState.fireEventForAnyCardsDiscardedBy(block: suspend MutableGameState.() -> Unit) {
    val discardBefore = discard.cards.toSet()
    block()
    val discardAfter = discard.cards.toSet()

    (discardAfter - discardBefore).takeIf { it.isNotEmpty() }?.let { discarded ->
        effects.processCardsDiscarded(discarded.toList())
    }
}

sealed class GameStateChange {
    suspend fun applyTo(state: MutableGameState) = state.apply()
    protected abstract suspend fun MutableGameState.apply()

    /**
     * Some changes don't need to be saved into history, e.g. passive VP calculations
     */
    open val transient: Boolean = false

    class GameStart : GameStateChange() {
        // Doesn't do anything; just a marker that this game has started
        override suspend fun MutableGameState.apply() = Unit
    }

    class ShuffleDiscardIntoDeck(var discardSize: Int = 0) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            discardSize = discard.cards.size
            move(discard.cards.shuffled(random()), deck)
            effects.processPileShuffed(deck)
        }
    }

    /**
     * @param requestedCount How many cards we want to draw this turn. If no value is specified, the user's handsize
     *   will be drawn. Note that we might not be able to draw this many cards, if the user's deck + discard pile don't
     *   have enough cards in them to fulfill the request. Check the size of [cards] to know how many cards were
     *   actually drawn.
     *
     * @param cards Will be set to the cards drawn by this command.
     */
    class Draw(val requestedCount: Int? = null, var cards: List<Card> = emptyList()) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            // Multiple draws can happen in a single turn thanks to card actions. Here, we only want to do some stuff
            // on the first draw per turn -- that is, the first "Draw" change after an "EndTurn"
            val isFirstDrawThisTurn = run {
                val prevChange = history
                    .asSequence()
                    .flatMap { changes -> changes.items.asSequence() }
                    .lastOrNull { it is Draw || it is EndTurn }
                prevChange !is Draw
            }

            var count = this@Draw.requestedCount ?: handSize
            if (deck.cards.size < count && discard.cards.isNotEmpty()) {
                apply(ShuffleDiscardIntoDeck())
            }

            count = count.coerceAtMost(deck.cards.size)
            deck.cards.take(count).let { cards ->
                move(cards, hand)
                this@Draw.cards = cards
            }

            if (isFirstDrawThisTurn) {
                effects.processTurnStarted()
            }
        }
    }

    class Play(val card: Card) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            require(hand.cards.firstOrNull { it.id == card.id } != null) {
                "You cannot play \"${card.template.name}\" as it is not in your hand."
            }

            if (card.isExpendable) {
                apply(MoveCard(card, graveyard))
            } else if (card.isSuspicious) {
                apply(MoveCard(card, jail))
            } else {
                apply(MoveCard(card, street))
            }

            // Apply upgrades *first*, as otherwise, playing a card may add an upgrade which shouldn't take effect until
            // a later turn.
            if (card.isDexterous) apply(AddGameAmount(GameProperty.CASH, 1))
            if (card.isArtful) apply(AddGameAmount(GameProperty.INFLUENCE, 1))
            if (card.isLucky) apply(AddGameAmount(GameProperty.LUCK, 1))
            if (card.isVeteran) {
                apply(Draw(1))
                enqueuers.expr.enqueue(this, "pile-move-to! \$discard (choose --required _ --prompt \"Discard a card.\" \$hand 1)")
                enqueuers.actionQueue.runEnqueuedActions()
            }

            // Trigger effects first. Once we play a card, it might install an additional effect, which we don't want
            // to immediately run against the card itself
            effects.processCardPlayed(card)
            fireEventForAnyCardsDiscardedBy {
                enqueuers.card.enqueuePlayActions(this, card)
                enqueuers.actionQueue.runEnqueuedActions()
            }
        }
    }

    fun GameState.MoveCard(card: Card, intoPile: Pile, listStrategy: ListStrategy = ListStrategy.BACK) =
        MoveCard(this, card, intoPile, listStrategy)

    class MoveCard(val card: Card, val fromPile: Pile?, val intoPile: Pile, val listStrategy: ListStrategy = ListStrategy.BACK) :
        GameStateChange() {

        constructor(state: GameState, card: Card, intoPile: Pile, listStrategy: ListStrategy = ListStrategy.BACK) :
                this(card, state.pileFor(card), intoPile, listStrategy)

        override suspend fun MutableGameState.apply() {
            move(card, intoPile, listStrategy)
        }
    }

    class MoveCards(val cards: Map<Pile?, List<Card>>, val intoPile: Pile, val listStrategy: ListStrategy = ListStrategy.BACK) :
        GameStateChange() {

        constructor(state: GameState, cards: List<Card>, intoPile: Pile, listStrategy: ListStrategy = ListStrategy.BACK) :
                this(cards.groupBy { card -> state.pileFor(card) }, intoPile, listStrategy)

        override suspend fun MutableGameState.apply() {
            move(cards.values.flatten(), intoPile, listStrategy)
        }
    }

    class Shuffle(val pile: Pile) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            (pile as MutablePile).cards.shuffle(random())
            effects.processPileShuffed(pile)
        }
    }

    class AddCardAmount(val property: CardProperty, val card: Card, val amount: Int) : GameStateChange() {
        override val transient: Boolean
            get() = when(property) {
                CardProperty.VP_PASSIVE -> true
                else -> false
            }

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

    class AddTrait(val card: Card, val traitType: TraitType) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            (card as MutableCard).traits.add(traitType)
        }
    }

    class RemoveTrait(val card: Card, val traitType: TraitType) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            (card as MutableCard).traits.remove(traitType)
        }
    }

    class AddBuildingAmount(val property: BuildingProperty, val building: Building, val amount: Int) : GameStateChange() {
        override val transient: Boolean
            get() = when(property) {
                BuildingProperty.VP_PASSIVE -> true
                else -> false
            }

        override suspend fun MutableGameState.apply() {
            when (property) {
                BuildingProperty.COUNTER -> (building as MutableBuilding).counter += amount
                BuildingProperty.VP -> (building as MutableBuilding).vpBase += amount
                BuildingProperty.VP_PASSIVE -> (building as MutableBuilding).vpPassive += amount
                else -> error("Can't change read-only building property $property")
            }
        }
    }

    class AddGameAmount(val property: GameProperty, var amount: Int) : GameStateChange() {
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

    class SetGameData(val key: String, val value: DataValue) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            data[key] = value
        }
    }

    class AddEffect(val effect: Effect<*>) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            effects.items.add(effect)
        }
    }

    class AddGameTweak(val tweak: Tweak.Game) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            tweaks.items.add(tweak)
        }
    }

    class AddShopTweak(val tweak: Tweak.Shop) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            shop.tweaks.items.add(tweak)
            when (tweak) {
                is Tweak.Shop.Prices -> shop.refreshPrices()
                is Tweak.Shop.Size -> shop.restock(restockAll = false)
                else -> Unit
            }
        }
    }

    class Buy(val card: Card, var soldOut: Boolean = false) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            require(shop.stock.asSequence().filterNotNull().firstOrNull { it.id == card.id } != null) {
                "Trying to buy ${card.template.name} but it's not a card in the shop"
            }

            val price = shop.priceFor(card)
            if (price > 0) {
                // Apply the payment, ensuring that this change shows up in the game report.
                apply(AddGameAmount(GameProperty.CASH, -price))
            }

            apply(MoveCard(card, if (card.isSwift) hand else street))

            soldOut = shop.remaining(card.template) == 0
        }
    }

    class RestockShop(var limitedInventory: Boolean = false, private val additionalFilter: suspend (CardTemplate) -> Boolean = { true }) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            shop.restock(additionalFilter = additionalFilter)
            effects.processShopRestocked()

            // Should be really rare, but can happen if the filter is strict OR if due to some unlucky infinite bug,
            // some user was able to buy all cards in the shop.
            limitedInventory = shop.stock.any { it == null }
        }
    }

    class UpgradeShop(var tier: Int = 0) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            shop.upgrade()
            tier = shop.tier
        }
    }

    class Build(val blueprint: Blueprint, val free: Boolean = false) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            require(blueprint in blueprints) { "You cannot build the blueprint \"${blueprint.name}\" as you don't own it." }

            if (!free) {
                if (blueprint.buildCost.cash > 0) {
                    apply(AddGameAmount(GameProperty.CASH, -blueprint.buildCost.cash))
                }
                if (blueprint.buildCost.influence > 0) {
                    apply(AddGameAmount(GameProperty.INFLUENCE, -blueprint.buildCost.influence))
                }
            }

            blueprints.remove(blueprint)
            val building = blueprint.build()
            buildings.add(building).also { buildings.sort() }

            enqueuers.building.enqueueInitActions(this, building)
            enqueuers.actionQueue.runEnqueuedActions()
        }
    }

   class Activate(val building: Building) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            val building = buildings.firstOrNull { it.id == building.id } ?: error(
                "You cannot activate the building \"${building.blueprint.name}\" as it hasn't been built yet."
            )
            require(!building.isActivated) { "You cannot activate the building \"${building.blueprint.name}\" as it has already been activated." }

            val cost = building.blueprint.activationCost
            if (cost.cash > 0) {
                apply(AddGameAmount(GameProperty.CASH, -cost.cash))
            }
            if (cost.influence > 0) {
                apply(AddGameAmount(GameProperty.INFLUENCE, -cost.influence))
            }
            if (cost.luck > 0) {
                apply(AddGameAmount(GameProperty.LUCK, -cost.luck))
            }

            building.isActivated = true

            // Run its activate actions.
            fireEventForAnyCardsDiscardedBy {
                enqueuers.building.enqueueActivateActions(this, building)
                enqueuers.actionQueue.runEnqueuedActions()
            }
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

            if (tweaks.consumeIsNotSet<Tweak.Game.KeepUnspent>()) {
                cash = 0
            }

            effects.processTurnEnded()
            move(street.cards, discard)
            move(hand.cards, discard)

            val shouldRestock = shop.tweaks.consumeIsNotSet<Tweak.Shop.Frozen>()

            buildings.forEach { it.isActivated = false }

            tweaks.notifyTurnEnded()
            shop.tweaks.notifyTurnEnded()

            // Restock AFTER tweaks are updated, as it might affect the shop size at the end of the turn
            if (shouldRestock) {
                shop.restock()
            }
        }
    }

    class GameOver : GameStateChange() {
        // Doesn't do anything; just a marker that this game is finished
        override suspend fun MutableGameState.apply() = Unit
    }
}