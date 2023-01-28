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
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.max

private fun <E> MutableList<E>.setTo(items: List<E>) {
    clear()
    addAll(items)
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

private fun <T, M: WrappingViewModel<T>> MutableList<M>.update(unwrapped: List<T>, create: (unwrapped: T) -> M) {
    val thisSize = size
    val unwrappedSize = unwrapped.size

    for (i in 0 until unwrappedSize) {
        if (i < thisSize) {
            this[i].update(unwrapped[i])
        } else {
            add(create(unwrapped[i]))
        }
    }
    // In case this is larger than the target list
    for (i in unwrappedSize until thisSize) {
        this.removeLast()
    }
}

private fun <T, M: WrappingViewModel<T>> MutableList<M?>.update(unwrapped: List<T?>, create: (unwrapped: T) -> M) {
    val thisSize = size
    val unwrappedSize = unwrapped.size

    for (i in 0 until unwrappedSize) {
        if (i < thisSize) {
            when {
                this[i] == null && unwrapped[i] != null -> this[i] = create(unwrapped[i]!!)
                this[i] != null && unwrapped[i] == null -> this[i] = null
                this[i] != null && unwrapped[i] != null -> this[i]!!.update(unwrapped[i]!!)
            }
        } else {
            this.add(unwrapped[i]?.let { create(it) })
        }
    }
    // In case this is larger than the target list
    for (i in unwrappedSize until thisSize) {
        this.removeLast()
    }
}

private fun MutableList<GameStateViewModel.CardViewModel>.setTo(cards: List<Card>) {
    this.update(cards) { c -> GameStateViewModel.CardViewModel(c) }
}

private fun MutableList<GameStateViewModel.CardViewModel?>.setTo(cards: List<Card?>) {
    this.update(cards) { c -> GameStateViewModel.CardViewModel(c) }
}
private fun MutableList<GameStateViewModel.BuildingViewModel>.setTo(buildings: List<Building>) {
    this.update(buildings) { b -> GameStateViewModel.BuildingViewModel(b) }
}

abstract class WrappingViewModel<T>(wrapped: T) {
    protected var wrapped = wrapped
        private set

    init {
        window.setTimeout({
            update(wrapped) // Give subclasses a chance to initialize their fields
        })
    }

    fun update(wrapped: T) {
        this.wrapped = wrapped
        onWrappedUpdated()
    }

    abstract fun onWrappedUpdated()
}

/**
 * Wrap a [GameState] instance with observable hooks which make it easy to be used by Compose.
 */
class GameStateViewModel(scope: CoroutineScope, private val events: Events, initialState: GameState) : GameState, WrappingViewModel<GameState>(initialState) {

    class CardViewModel(card: Card) : Card, WrappingViewModel<Card>(card) {
        override val id get() = wrapped.id
        override val template get() = wrapped.template

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

        override fun compareTo(other: Card) = wrapped.compareTo(other)

        override fun onWrappedUpdated() {
            _vpBase = wrapped.vpBase
            _vpPassive = wrapped.vpPassive
            _counter = wrapped.counter

            _traits.setTo(wrapped.traits)
            _upgrades.setTo(wrapped.upgrades)
        }
    }

    class BuildingViewModel(building: Building) : Building, WrappingViewModel<Building>(building) {
        override val id get() = wrapped.id
        override val blueprint get() = wrapped.blueprint

        private var _vpBase by mutableStateOf(0)
        override val vpBase get() = _vpBase
        private var _vpPassive by mutableStateOf(0)
        override val vpPassive get() = _vpPassive
        private var _counter by mutableStateOf(0)
        override val counter get() = _counter

        private var _isActivated by mutableStateOf(false)
        override val isActivated get() = _isActivated

        override fun compareTo(other: Building) = wrapped.compareTo(other)

        override fun onWrappedUpdated() {
            _vpBase = wrapped.vpBase
            _vpPassive = wrapped.vpPassive
            _counter = wrapped.counter

            _isActivated = wrapped.isActivated
        }
    }

    class PileViewModel(pile: Pile) : Pile, WrappingViewModel<Pile>(pile) {
        override val id get() = wrapped.id

        private val _cards = mutableStateListOf<CardViewModel>()
        override val cards: List<Card> = _cards

        override fun onWrappedUpdated() {
            _cards.setTo(wrapped.cards)
        }
    }

    class ShopViewModel(shop: Shop) : Shop, WrappingViewModel<Shop>(shop) {
        private var _tier by mutableStateOf(shop.tier)
        override val tier get() = _tier

        private val _stock = mutableStateListOf<CardViewModel?>()
        override val stock: List<Card?> = _stock

        private val _prices = mutableStateMapOf<Uuid, Int>()
        override val prices: Map<Uuid, Int> = _prices

        private val _tweaks = TweaksViewModel(shop.tweaks)
        override val tweaks: Tweaks<Tweak.Shop> = _tweaks

        override val bought: Map<String, Int> get() = wrapped.bought
        override val rarities: List<Rarity> get() = wrapped.rarities

        override fun onWrappedUpdated() {
            _tier = wrapped.tier

            _stock.setTo(wrapped.stock)
            _prices.setTo(wrapped.prices)
            _tweaks.update(wrapped.tweaks)
        }
    }

    class EffectsViewModel(effects: Effects): Effects, WrappingViewModel<Effects>(effects) {
        private val _items = mutableStateListOf<Effect<*>>()
        override val items: List<Effect<*>> = _items

        override fun onWrappedUpdated() {
            _items.setTo(wrapped.items)
        }
    }

    class TweaksViewModel<T: Tweak>(tweaks: Tweaks<T>): Tweaks<T>, WrappingViewModel<Tweaks<T>>(tweaks) {
        private val _items = mutableStateListOf<T>()
        override val items: List<T> = _items

        override fun collect(predicate: (Tweak) -> Boolean) = wrapped.collect(predicate)

        override fun onWrappedUpdated() {
            _items.setTo(wrapped.items)
        }
    }

    override val id get() = wrapped.id
    override val random get() = wrapped.random
    override val features get() = wrapped.features

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
    override val numTurns get() = wrapped.numTurns

    private var _handSize by mutableStateOf(initialState.handSize)
    override val handSize get() = wrapped.handSize

    private val _shop = ShopViewModel(initialState.shop)
    override val shop: Shop = _shop

    private val _deck = PileViewModel(wrapped.deck)
    override val deck: Pile = _deck

    private val _hand = PileViewModel(wrapped.hand)
    override val hand: Pile = _hand

    private val _street = PileViewModel(wrapped.street)
    override val street: Pile = _street

    private val _discard = PileViewModel(wrapped.discard)
    override val discard: Pile = _discard

    private val _jail = PileViewModel(wrapped.jail)
    override val jail: Pile = _jail

    private val _graveyard = PileViewModel(wrapped.graveyard)
    override val graveyard: Pile = _graveyard

    private val _blueprints = mutableStateListOf<Blueprint>()
    override val blueprints: List<Blueprint> get() = _blueprints

    private val _buildings = mutableStateListOf<BuildingViewModel>()
    override val buildings: List<Building> get() = _buildings

    private val _effects = EffectsViewModel(wrapped.effects)
    override val effects: Effects = wrapped.effects
    private val _tweaks = TweaksViewModel(wrapped.tweaks)
    override val tweaks: Tweaks<Tweak.Game> = wrapped.tweaks

    override val data: Map<String, DataValue> get() = wrapped.data

    private val _history = mutableStateListOf<GameStateChanges>()
    override val history: List<GameStateChanges> = _history

    override fun pileFor(card: Card): Pile? = wrapped.pileFor(card)
    override fun canActivate(building: Building) = wrapped.canActivate(building)


    init {
        scope.launch {
            events.collect { evt ->
                when (evt) {
                    is Event.GameStateUpdated -> {
                        update(evt.ctx.state)
                    }

                    else -> {}
                }
            }
        }
    }

    override fun onWrappedUpdated() {
        _turn = wrapped.turn

        _cash = wrapped.cash
        _influence = wrapped.influence
        _luck = wrapped.luck

        _vp = wrapped.vp

        _numTurns = wrapped.numTurns
        _handSize = wrapped.handSize

        _shop.update(wrapped.shop)

        _deck.update(wrapped.deck)
        _hand.update(wrapped.hand)
        _street.update(wrapped.street)
        _discard.update(wrapped.discard)
        _jail.update(wrapped.jail)
        _graveyard.update(wrapped.graveyard)

        _blueprints.setTo(wrapped.blueprints)
        _buildings.setTo(wrapped.buildings)

        _effects.update(wrapped.effects)
        _tweaks.update(wrapped.tweaks)

        _history.setTo(wrapped.history)
    }
}