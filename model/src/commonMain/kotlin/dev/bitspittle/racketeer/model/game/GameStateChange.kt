package dev.bitspittle.racketeer.model.game

import dev.bitspittle.limp.types.ListStrategy
import dev.bitspittle.racketeer.model.action.enqueue
import dev.bitspittle.racketeer.model.building.*
import dev.bitspittle.racketeer.model.card.*
import dev.bitspittle.racketeer.model.effect.*
import dev.bitspittle.racketeer.model.pile.Pile
import dev.bitspittle.racketeer.model.serialization.DataValue
import dev.bitspittle.racketeer.model.shop.priceFor
import dev.bitspittle.racketeer.model.shop.remaining

/**
 * Check if the current card is safe to reference in the context of a change.
 *
 * If not, then it means that this change would crash if you saved your game and then attempted to reload it.
 */
private fun Card.isSafeToSerialize(state: GameState): Boolean {
    return state.pileFor(this) != null || state.shop.stock.contains(this)
}

@Suppress("CanSealedSubClassBeObject")
sealed class GameStateChange {
    suspend fun applyTo(state: MutableGameState) = state.apply()
    fun shouldSave(state: GameState) = !state.ignoreSerialization()
    protected abstract suspend fun MutableGameState.apply()
    protected open fun GameState.ignoreSerialization() = false

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
                val prevChange = allChanges.lastOrNull { it !== this@Draw && (it is Draw || it is EndTurn) }
                prevChange !is Draw
            }

            var count = this@Draw.requestedCount ?: handSize
            if (deck.cards.size < count && discard.cards.isNotEmpty()) {
                addChange(ShuffleDiscardIntoDeck())
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
                addChange(MoveCard(card, graveyard))
            } else if (card.isSuspicious) {
                addChange(MoveCard(card, jail))
            } else {
                addChange(MoveCard(card, street))
            }

            // Apply upgrades *first*, as otherwise, playing a card may add an upgrade which shouldn't take effect until
            // a later turn.
            if (card.isDexterous) addChange(AddGameAmount(GameProperty.CASH, 1))
            if (card.isArtful) addChange(AddGameAmount(GameProperty.INFLUENCE, 1))
            if (card.isLucky) addChange(AddGameAmount(GameProperty.LUCK, 1))
            if (card.isVeteran) {
                if (deck.cards.isNotEmpty() || discard.cards.isNotEmpty()) {
                    addChange(Draw(1))
                    enqueuers.expr.enqueue(
                        this,
                        "pile-move-to! \$discard (choose --required _ --prompt \"Discard a card.\" \$hand 1)"
                    )
                    enqueuers.actionQueue.runEnqueuedActions()
                }
            }

            // Trigger effects first. Once we play a card, it might install an additional effect, which we don't want
            // to immediately run against the card itself
            effects.processCardPlayed(card)
            enqueuers.card.enqueuePlayActions(this, card)
            enqueuers.actionQueue.runEnqueuedActions()
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
            if (intoPile.id == discard.id) {
                effects.processCardsDiscarded(listOf(card))
            }
        }
    }

    class MoveCards(val cards: Map<Pile?, List<Card>>, val intoPile: Pile, val listStrategy: ListStrategy = ListStrategy.BACK) :
        GameStateChange() {

        constructor(state: GameState, cards: List<Card>, intoPile: Pile, listStrategy: ListStrategy = ListStrategy.BACK) :
                this(cards.groupBy { card -> state.pileFor(card) }, intoPile, listStrategy)

        override suspend fun MutableGameState.apply() {
            val cards = cards.values.flatten()
            if (cards.isNotEmpty()) {
                move(cards, intoPile, listStrategy)
                if (intoPile.id == discard.id) {
                    effects.processCardsDiscarded(cards)
                }
            }
        }
    }

    class Shuffle(val pile: Pile) : GameStateChange() {
        override suspend fun MutableGameState.apply() {
            pile.toMutablePile(this).cards.shuffle(random())
            effects.processPileShuffed(pile)
        }
    }

    class AddCardAmount(val property: CardProperty, val card: Card, val amount: Int) : GameStateChange() {
        override fun GameState.ignoreSerialization(): Boolean {
            return property == CardProperty.VP_PASSIVE || !card.isSafeToSerialize(this)
        }

        override suspend fun MutableGameState.apply() {
            when (property) {
                CardProperty.COUNTER -> card.toMutableCard(this).counter += amount
                CardProperty.VP -> card.toMutableCard(this).vpBase += amount
                CardProperty.VP_PASSIVE -> card.toMutableCard(this).vpPassive += amount
                else -> error("Can't change read-only card property $property")
            }
        }
    }

    class UpgradeCard(val card: Card, val upgradeType: UpgradeType) : GameStateChange() {
        override fun GameState.ignoreSerialization(): Boolean {
            return !card.isSafeToSerialize(this)
        }

        override suspend fun MutableGameState.apply() {
            card.toMutableCard(this).upgrades.add(upgradeType)
        }
    }

    class AddTrait(val card: Card, val traitType: TraitType) : GameStateChange() {
        override fun GameState.ignoreSerialization(): Boolean {
            return !card.isSafeToSerialize(this)
        }

        override suspend fun MutableGameState.apply() {
            card.toMutableCard(this).traits.add(traitType)
        }
    }

    class RemoveTrait(val card: Card, val traitType: TraitType) : GameStateChange() {
        override fun GameState.ignoreSerialization(): Boolean {
            return !card.isSafeToSerialize(this)
        }

        override suspend fun MutableGameState.apply() {
            card.toMutableCard(this).traits.remove(traitType)
        }
    }

    class AddBuildingAmount(val property: BuildingProperty, val building: Building, val amount: Int) : GameStateChange() {
        override fun GameState.ignoreSerialization(): Boolean {
            return property == BuildingProperty.VP_PASSIVE
        }

        override suspend fun MutableGameState.apply() {
            when (property) {
                BuildingProperty.COUNTER -> building.toMutableBuilding(this).counter += amount
                BuildingProperty.VP -> building.toMutableBuilding(this).vpBase += amount
                BuildingProperty.VP_PASSIVE -> building.toMutableBuilding(this).vpPassive += amount
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
                addChange(AddGameAmount(GameProperty.CASH, -price))
            }

            addChange(MoveCard(card, street))

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
                    addChange(AddGameAmount(GameProperty.CASH, -blueprint.buildCost.cash))
                }
                if (blueprint.buildCost.influence > 0) {
                    addChange(AddGameAmount(GameProperty.INFLUENCE, -blueprint.buildCost.influence))
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
                addChange(AddGameAmount(GameProperty.CASH, -cost.cash))
            }
            if (cost.influence > 0) {
                addChange(AddGameAmount(GameProperty.INFLUENCE, -cost.influence))
            }
            if (cost.luck > 0) {
                addChange(AddGameAmount(GameProperty.LUCK, -cost.luck))
            }

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
                addChange(GameOver())
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