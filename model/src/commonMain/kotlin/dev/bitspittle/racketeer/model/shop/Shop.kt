package dev.bitspittle.racketeer.model.shop

import com.benasher44.uuid.Uuid
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.model.card.featureTypes
import dev.bitspittle.racketeer.model.game.Feature
import dev.bitspittle.racketeer.model.random.CopyableRandom

interface Shop {
    val tier: Int
    val stock: List<Card?>
    val exclusions: List<Exclusion>
}

class MutableShop internal constructor(
    private val random: CopyableRandom,
    private val allCards: List<CardTemplate>,
    private val features: Set<Feature.Type>,
    private val shopSizes: List<Int>,
    private val tierFrequencies: List<Int>,
    private val rarityFrequencies: List<Int>,
    tier: Int,
    override val stock: MutableList<Card?>,
    override val exclusions: MutableList<Exclusion>,
) : Shop {
    constructor(
        random: CopyableRandom,
        allCards: List<CardTemplate>,
        features: Set<Feature.Type>,
        shopSizes: List<Int>,
        tierFrequencies: List<Int>,
        rarityFrequencies: List<Int>
    ) : this(
        random,
        allCards,
        features,
        shopSizes,
        tierFrequencies,
        rarityFrequencies,
        0,
        mutableListOf(),
        mutableListOf(),
    ) {
        handleRestock(restockAll = true, filterAllCards { true })
    }

    override var tier: Int = tier
        private set

    private fun handleRestock(restockAll: Boolean, possibleNewStock: List<CardTemplate>) {
        if (!restockAll && stock.size == shopSizes[tier]) return // Shop is full; incremental restock fails
        if (restockAll) {
            stock.clear()
        }

        // Create an uber stock, which has all cards repeated a bunch of times as a lazy way to implement random
        // frequency distribution
        fun createUberStock(): MutableList<CardTemplate> {
            val uberStock = mutableListOf<CardTemplate>()
            possibleNewStock.forEach { card ->
                repeat(tierFrequencies[card.tier] * rarityFrequencies[card.rarity]) { uberStock.add(card) }
            }
            return uberStock
        }

        val uberStock = createUberStock()

        var numCardsToStock = shopSizes[tier] - stock.size
        while (numCardsToStock > 0 && uberStock.isNotEmpty()) {
            val template = uberStock.random(random())
            uberStock.removeAll { it === template } // Remove all instances, to encourage more variety
            stock.add(template.instantiate())

            numCardsToStock--
        }
        repeat(numCardsToStock) { stock.add(null) }
    }

    private inline fun filterAllCards(additionalFilter: (CardTemplate) -> Boolean): List<CardTemplate> {
        return allCards
            .filter { card ->
                card.cost > 0
                        && card.tier <= this.tier
                        && features.containsAll(card.featureTypes)
                        && additionalFilter(card) }
    }

    suspend fun restock(restockAll: Boolean = true, additionalFilter: suspend (CardTemplate) -> Boolean = { true }) {
        handleRestock(
            restockAll,
            filterAllCards { card -> additionalFilter(card) && exclusions.none { exclude -> exclude(card) } })
    }

    fun addExclusion(exclusion: Exclusion) { exclusions.add(exclusion) }

    fun notifyBought(cardId: Uuid) {
        for (i in stock.indices) {
            stock[i]?.run {
                if (this.id == cardId) {
                    stock[i] = null
                    return
            }}
        }
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
        rarityFrequencies,
        tier,
        stock.toMutableList(),
        exclusions.toMutableList()
    )
}
