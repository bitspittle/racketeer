package dev.bitspittle.racketeer.model.shop

import com.benasher44.uuid.Uuid
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.model.card.Rarity
import dev.bitspittle.racketeer.model.card.featureTypes
import dev.bitspittle.racketeer.model.effect.*
import dev.bitspittle.racketeer.model.game.Feature
import dev.bitspittle.racketeer.model.random.CopyableRandom
import kotlin.math.max
import kotlin.random.Random

interface Shop {
    val tier: Int
    val stock: List<Card?>
    /**
     * A map of stocked cards (represented by ID) to their price, which can be different from the card's cost if
     * various tweaks are set.
     */
    val prices: Map<Uuid, Int>

    val tweaks: Tweaks<Tweak.Shop>

    /**
     * A count of how many times a player bought this card, by name.
     *
     * If the card is not in this map, then it means one hasn't been bought yet, at which point you can use
     * [Rarity.shopCount] instead.
     */
    val bought: Map<String, Int>

    /**
     * Rarity information which can be useful for querying card counts in the shop.
     */
    val rarities: List<Rarity>
}

/**
 * Check the price of a card, which MUST be in the shop or else this throws an [IllegalArgumentException].
 */
fun Shop.priceFor(card: Card): Int {
    return prices[card.id]
        ?: throw IllegalArgumentException("Tried to get the price of card \"${card.template.name}\" (${card.id}) which is not in the shop.")
}


fun Shop.remaining(card: CardTemplate): Int {
    val maxStock = card.shopCount ?: rarities[card.rarity].shopCount
    return maxStock - (bought[card.name] ?: 0)
}

class MutableShop internal constructor(
    private val random: CopyableRandom,
    private val allCards: List<CardTemplate>,
    private val features: Set<Feature.Type>,
    private val shopSizes: List<Int>,
    private val tierFrequencies: List<Int>,
    override val rarities: List<Rarity>,
    tier: Int,
    override val stock: MutableList<Card?>,
    override val prices: MutableMap<Uuid, Int>,
    override val tweaks: MutableTweaks<Tweak.Shop>,
    override val bought: MutableMap<String, Int>,
) : Shop {
    constructor(
        random: CopyableRandom,
        allCards: List<CardTemplate>,
        features: Set<Feature.Type>,
        shopSizes: List<Int>,
        tierFrequencies: List<Int>,
        rarities: List<Rarity>,
    ) : this(
        random,
        allCards,
        features,
        shopSizes,
        tierFrequencies,
        rarities,
        0,
        mutableListOf(),
        mutableMapOf(),
        MutableTweaks(),
        mutableMapOf(),
    ) {
        handleRestock(restockAll = true, filterAllCards { true })
    }

    override var tier: Int = tier
        private set

    private val shopSize: Int get() {
        return shopSizes[tier] + tweaks.consumeCollectInstances<Tweak.Shop.Size>().sumOf { it.amount }
    }

    private fun handleRestock(restockAll: Boolean, possibleNewStock: List<CardTemplate>) {
        val shopSize = shopSize
        // The shop size could have shrunk if a tweak increasing the shop size ran out
        while (stock.size > shopSize) stock.removeLast()

        if (!restockAll && stock.size == shopSize) return // Shop is full; incremental restock fails
        if (restockAll) {
            stock.clear()
            prices.clear()
        }

        // Create an uber stock, which has all cards repeated a bunch of times as a lazy way to implement random
        // frequency distribution
        fun createUberStock(): MutableList<CardTemplate> {
            val uberStock = mutableListOf<CardTemplate>()
            possibleNewStock.forEach { card ->
                repeat(tierFrequencies[card.tier] * rarities[card.rarity].cardFrequency) { uberStock.add(card) }
            }
            return uberStock
        }

        val uberStock = createUberStock()

        var numCardsToStock = shopSize - stock.size
        val random = random()
        while (numCardsToStock > 0 && uberStock.isNotEmpty()) {
            val template = uberStock.random(random)
            uberStock.removeAll { it === template } // Remove all instances, to encourage more variety
            val card = template.instantiate()
            stock.add(card)
            prices[card.id] = calculatePriceFor(card, random)

            numCardsToStock--
        }

        // It should be almost impossible to hit this, but we can run out of stock as it is not unlimited.
        repeat(numCardsToStock) { stock.add(null) }
    }

    private inline fun filterAllCards(additionalFilter: (CardTemplate) -> Boolean): List<CardTemplate> {
        return allCards
            .filter { card ->
                card.cost > 0
                        && card.tier <= this.tier
                        && features.containsAll(card.featureTypes)
                        && additionalFilter(card)
            }
    }

    suspend fun restock(restockAll: Boolean = true, additionalFilter: suspend (CardTemplate) -> Boolean = { true }) {
        handleRestock(
            restockAll,
            filterAllCards { card -> remaining(card) > 0 && additionalFilter(card) }
        )
    }

    private fun calculatePriceFor(card: Card, random: Random): Int {
        return max(0, card.template.cost +
                tweaks.collectInstances<Tweak.Shop.Prices>().sumOf { it.amount.random(random) }
        )
    }

    fun refreshPrices() {
        prices.clear()
        val random = random()
        stock.asSequence().filterNotNull().forEach { card ->
            prices[card.id] = calculatePriceFor(card, random)
        }

        tweaks.consumeCollectInstances<Tweak.Shop.Prices>()
    }

    fun notifyOwned(cardId: Uuid) {
        for (i in stock.indices) {
            stock[i]?.run {
                if (this.id == cardId) {
                    bought[this.template.name] = bought.getOrPut(this.template.name) { 0 } + 1
                        // A card should not have been put up for sale if we already sold too many of them
                        .also { check(remaining(this.template) >= 0) }

                    stock[i] = null
                    return
            }}
        }

        prices.remove(cardId)
    }

    suspend fun upgrade(): Boolean {
        if (tier >= shopSizes.size - 1) return false

        ++tier
        // New slot should ALWAYS contain a card from the new tier
        restock(restockAll = false) { card -> card.tier == tier }
        return true
    }

    fun copy(random: CopyableRandom = this.random.copy()) = MutableShop(
        random,
        allCards,
        features,
        shopSizes,
        tierFrequencies,
        rarities,
        tier,
        stock.toMutableList(),
        prices.toMutableMap(),
        tweaks.copy(),
        bought.toMutableMap(),
    )
}
