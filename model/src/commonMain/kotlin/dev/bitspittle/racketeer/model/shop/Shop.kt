package dev.bitspittle.racketeer.model.shop

import com.benasher44.uuid.Uuid
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate
import kotlin.random.Random

interface Shop {
    val tier: Int
    val stock: List<Card?>
    fun upgrade(): Boolean
    fun restockNow(restockAll: Boolean = true, additionalFilter: (CardTemplate) -> Boolean = { true }): Boolean
    suspend fun restock(restockAll: Boolean = true, additionalFilter: suspend (CardTemplate) -> Boolean = { true }): Boolean
}

class MutableShop private constructor(
    private val random: Random,
    private val allCards: List<CardTemplate>,
    private val shopSizes: List<Int>,
    private val tierFrequencies: List<Int>,
    private val rarityFrequencies: List<Int>,
    tier: Int,
    override val stock: MutableList<Card?>) : Shop {
    constructor(random: Random, allCards: List<CardTemplate>, shopSizes: List<Int>, tierFrequencies: List<Int>, rarityFrequencies: List<Int>) : this(
        random,
        allCards,
        shopSizes,
        tierFrequencies,
        rarityFrequencies,
        0,
        mutableListOf()
    ) {
        restockNow()
    }

    override var tier: Int = tier
        private set

    private fun handleRestock(restockAll: Boolean, possibleNewStock: MutableMap<Int, MutableList<CardTemplate>>): Boolean {
        if (!restockAll && stock.size == shopSizes[tier]) return false // Shop is full; incremental restock fails

        if (restockAll) {
            stock.clear()
        }

        var numCardsToStock = shopSizes[tier] - stock.size

        // This should never happen, but fail fast in case game data is bad!
        require(possibleNewStock.values.sumOf { it.size } >= numCardsToStock) {
            "There are not enough cards defined to restock the shop at tier ${tier + 1}."
        }

        // Don't include frequency distribution entries for tiers we don't yet support
        val frequencyBuckets = FrequencyBuckets(tierFrequencies.take(tier + 1))
        val rarityBuckets = FrequencyBuckets(rarityFrequencies)
        while (numCardsToStock > 0) {
            val tier = frequencyBuckets.pickRandomBucket(random)
            // Choices for a tier might be unavailable even if we wanted to randomly pick it, due to the passed in
            // filter. At that point, just give up and keep trying until we get a tier that works
            val choicesForThisTier = possibleNewStock[tier]?.takeIf { cards -> cards.isNotEmpty() } ?: continue

            // Same thing we said above -- except applied to card rarity
            var chosenCard: CardTemplate? = null
            while (chosenCard == null) {
                val rarity = rarityBuckets.pickRandomBucket(random)
                chosenCard = choicesForThisTier.filter { it.rarity == rarity }.takeIf { it.isNotEmpty() }?.random(random)
            }

            stock.add(chosenCard.instantiate())
            choicesForThisTier.remove(chosenCard)
            numCardsToStock--
        }

        return true
    }

    private inline fun filterAllCards(additionalFilter: (CardTemplate) -> Boolean): MutableMap<Int, MutableList<CardTemplate>> {
        return allCards
            .filter { it.cost > 0 && it.tier <= this.tier && additionalFilter(it) }
            .groupByTo(mutableMapOf()) { it.tier }
    }

    override fun restockNow(restockAll: Boolean, additionalFilter: (CardTemplate) -> Boolean): Boolean {
        return handleRestock(restockAll, filterAllCards(additionalFilter))
    }

    override suspend fun restock(restockAll: Boolean, additionalFilter: suspend (CardTemplate) -> Boolean): Boolean {
        return handleRestock(restockAll, filterAllCards { additionalFilter(it) })
    }

    fun remove(cardId: Uuid) {
        for (i in stock.indices) {
            stock[i]?.run {
                if (this.id == cardId) {
                    stock[i] = null
                    return
            }}
        }
    }

    override fun upgrade(): Boolean {
        if (tier >= shopSizes.size - 1) return false

        ++tier
        // New slot should ALWAYS contain a card from the new tier
        restockNow(restockAll = false) { card -> card.tier == tier }
        return true
    }

    fun copy() = MutableShop(random, allCards, shopSizes, tierFrequencies, rarityFrequencies, tier, stock.toMutableList())
}
