package dev.bitspittle.racketeer.site.viewmodel

import androidx.compose.runtime.*
import com.benasher44.uuid.Uuid
import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.building.Building
import dev.bitspittle.racketeer.model.card.*
import dev.bitspittle.racketeer.model.effect.Effect
import dev.bitspittle.racketeer.model.effect.Effects
import dev.bitspittle.racketeer.model.effect.Tweak
import dev.bitspittle.racketeer.model.effect.Tweaks
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.game.GameStateChanges
import dev.bitspittle.racketeer.model.pile.Pile
import dev.bitspittle.racketeer.model.serialization.DataValue
import dev.bitspittle.racketeer.model.shop.Shop
import dev.bitspittle.racketeer.site.model.Event
import dev.bitspittle.racketeer.site.model.Events
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private fun <E> MutableList<E>.setTo(items: Iterable<E>) {
    this.clear()
    this.addAll(items)
}

private fun <K, V> MutableMap<K, V>.setTo(map: Map<K, V>) {
    putAll(map)
    keys.toList().forEach { k ->
        if (k !in map) remove(k)
    }
}


private fun <E> MutableMap<E, Unit>.setTo(items: Set<E>) {
    this.setTo(items.associateWith { })
}

private fun List<Card>.toViewModelList() = this.map { c -> GameStateViewModel.CardViewModel(c) }
private fun List<Card?>.toViewModelList() = this.map { c -> c?.let { GameStateViewModel.CardViewModel(it) } }
private fun List<Building>.toViewModelList() = this.map { b -> GameStateViewModel.BuildingViewModel(b) }

class GameStateViewModel(scope: CoroutineScope, private val events: Events, initialState: GameState) : GameState {

    class CardViewModel(card: Card) : Card {
        private lateinit var _card: Card

        override val id get() = _card.id
        override val template get() = _card.template

        private var _vpBase by mutableStateOf(0)
        override val vpBase get() = _vpBase
        private var _vpPassive by mutableStateOf(0)
        override val vpPassive get() = _vpPassive
        private var _counter by mutableStateOf(0)
        override val counter get() = _counter

        private val _traits = mutableStateMapOf<TraitType, Unit>()
        override val traits: Set<TraitType> get() = _traits.keys

        private val _upgrades = mutableStateMapOf<UpgradeType, Unit>()
        override val upgrades: Set<UpgradeType> get() = _upgrades.keys

        override fun compareTo(other: Card) = _card.compareTo(other)

        init {
            update(card)
        }

        fun update(card: Card) {
            _card = card

            _vpBase = card.vpBase
            _vpPassive = card.vpPassive
            _counter = card.counter

            _traits.setTo(card.traits)
            _upgrades.setTo(card.upgrades)
        }
    }

    class BuildingViewModel(building: Building) : Building {
        private lateinit var _building: Building

        override val id get() = _building.id
        override val blueprint get() = _building.blueprint

        private var _vpBase by mutableStateOf(0)
        override val vpBase get() = _vpBase
        private var _vpPassive by mutableStateOf(0)
        override val vpPassive get() = _vpPassive
        private var _counter by mutableStateOf(0)
        override val counter get() = _counter

        private var _isActivated by mutableStateOf(false)
        override val isActivated get() = _isActivated

        override fun compareTo(other: Building) = _building.compareTo(other)

        init {
            update(building)
        }

        fun update(building: Building) {
            _building = building

            _vpBase = building.vpBase
            _vpPassive = building.vpPassive
            _counter = building.counter

            _isActivated = building.isActivated
        }
    }

    class PileViewModel(pile: Pile) : Pile {
        private lateinit var _pile: Pile

        override val id get() = _pile.id

        private val _cards = mutableStateListOf<Card>()
        override val cards: List<Card> = _cards

        init {
            update(pile)
        }

        fun update(pile: Pile) {
            _pile = pile

            _cards.setTo(pile.cards.toViewModelList())
        }
    }

    class ShopViewModel(shop: Shop) : Shop {
        private lateinit var _shop: Shop

        private var _tier by mutableStateOf(shop.tier)
        override val tier get() = _tier

        private val _stock = mutableStateListOf<Card?>()
        override val stock: List<Card?> = _stock

        private val _prices = mutableStateMapOf<Uuid, Int>()
        override val prices: Map<Uuid, Int> = _prices

        private val _tweaks = TweaksViewModel(shop.tweaks)
        override val tweaks: Tweaks<Tweak.Shop> = _tweaks

        override val bought: Map<String, Int> get() = _shop.bought
        override val rarities: List<Rarity> get() = _shop.rarities

        init {
            update(shop)
        }

        fun update(shop: Shop) {
            _shop = shop

            _tier = shop.tier

            _stock.setTo(shop.stock.toViewModelList())
            _prices.setTo(shop.prices)
            _tweaks.update(shop.tweaks)
        }
    }

    class EffectsViewModel(effects: Effects): Effects {
        private lateinit var _effects: Effects

        private val _items = mutableStateListOf<Effect<*>>()
        override val items: List<Effect<*>> = _items

        init {
            update(effects)
        }

        fun update(effects: Effects) {
            _effects = effects

            _items.setTo(effects.items)
        }
    }

    class TweaksViewModel<T: Tweak>(tweaks: Tweaks<T>): Tweaks<T> {
        private lateinit var _tweaks: Tweaks<T>

        private val _items = mutableStateListOf<T>()
        override val items: List<T> = _items

        override fun collect(predicate: (Tweak) -> Boolean) = _tweaks.collect(predicate)

        init {
            update(tweaks)
        }

        fun update(tweaks: Tweaks<T>) {
            _tweaks = tweaks

            _items.setTo(tweaks.items)
        }
    }

    private var _state = initialState

    override val id get() = _state.id
    override val random get() = _state.random
    override val features get() = _state.features

    private var _turn by mutableStateOf(initialState.turn)
    override val turn get() = _turn

    private var _cash by mutableStateOf(initialState.cash)
    override val cash get() = _cash

    private var _influence by mutableStateOf(initialState.influence)
    override val influence get() = _influence

    private var _luck by mutableStateOf(initialState.luck)
    override val luck get() = _luck

    private var _vp by mutableStateOf(initialState.vp)
    override val vp get() = _vp

    private var _numTurns by mutableStateOf(initialState.numTurns)
    override val numTurns get() = _state.numTurns

    private var _handSize by mutableStateOf(initialState.handSize)
    override val handSize get() = _state.handSize

    private val _shop = ShopViewModel(initialState.shop)
    override val shop: Shop = _shop

    private val _deck = PileViewModel(_state.deck)
    override val deck: Pile = _deck

    private val _hand = PileViewModel(_state.hand)
    override val hand: Pile = _hand

    private val _street = PileViewModel(_state.street)
    override val street: Pile = _street

    private val _discard = PileViewModel(_state.discard)
    override val discard: Pile = _discard

    private val _jail = PileViewModel(_state.jail)
    override val jail: Pile = _jail

    private val _graveyard = PileViewModel(_state.graveyard)
    override val graveyard: Pile = _graveyard

    private val _blueprints = mutableStateListOf<Blueprint>().apply { addAll(_state.blueprints) }
    override val blueprints: List<Blueprint> get() = _blueprints

    private val _buildings = mutableStateListOf<Building>().apply { addAll(_state.buildings) }
    override val buildings: List<Building> get() = _buildings

    private val _effects = EffectsViewModel(_state.effects)
    override val effects: Effects = _state.effects
    private val _tweaks = TweaksViewModel(_state.tweaks)
    override val tweaks: Tweaks<Tweak.Game> = _state.tweaks

    override val data: Map<String, DataValue> get() = _state.data

    private val _history = mutableStateListOf<GameStateChanges>()
    override val history: List<GameStateChanges> = _history

    override fun pileFor(card: Card): Pile? = _state.pileFor(card)
    override fun canActivate(building: Building) = _state.canActivate(building)

    init {
        scope.launch {
            events.collect { evt ->
                when (evt) {
                    is Event.GameStateUpdated -> {
                        _state = evt.ctx.state

                        _turn = _state.turn

                        _cash = _state.cash
                        _influence = _state.influence
                        _luck = _state.luck

                        _vp = _state.vp

                        _numTurns = _state.numTurns
                        _handSize = _state.handSize

                        _shop.update(_state.shop)

                        _deck.update(_state.deck)
                        _hand.update(_state.hand)
                        _street.update(_state.street)
                        _discard.update(_state.discard)
                        _jail.update(_state.jail)
                        _graveyard.update(_state.graveyard)

                        _blueprints.setTo(_state.blueprints)
                        _buildings.setTo(_state.buildings)

                        _effects.update(_state.effects)
                        _tweaks.update(_state.tweaks)

                        _history.setTo(_state.history)
                    }

                    else -> {}
                }
            }
        }
    }
}