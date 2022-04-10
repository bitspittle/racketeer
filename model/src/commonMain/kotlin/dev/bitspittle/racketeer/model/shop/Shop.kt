package dev.bitspittle.racketeer.model.shop

import com.benasher44.uuid.Uuid
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate
import kotlin.random.Random

interface Shop {
    val tier: Int
    val stock: List<Card?>
}

class MutableShop private constructor(
    private val random: Random,
    private val allCards: List<CardTemplate>,
    private val shopSizes: List<Int>,
    private val frequencyDistribution: List<Int>,
    tier: Int,
    override val stock: MutableList<Card?>) : Shop {
    constructor(random: Random, allCards: List<CardTemplate>, shopSizes: List<Int>, frequencyDistribution: List<Int>) : this(
        random,
        allCards,
        shopSizes,
        frequencyDistribution,
        0,
        mutableListOf()
    ) {
        restock(true)
    }

    override var tier: Int = tier
        private set

    fun restock(restockAll: Boolean = true, additionalFilter: (CardTemplate) -> Boolean = { true }): Boolean {
        if (!restockAll && stock.size == shopSizes[tier]) return false // Shop is full; incremental restock fails

        val prevStock = stock.asSequence().filterNotNull().map { it.template }.toSet()
        val possibleNewStock =
            allCards.filter { it.cost > 0 && it.tier <= this.tier && !prevStock.contains(it) && additionalFilter(it) }
                .groupByTo(mutableMapOf()) { it.tier }

        if (restockAll) {
            stock.clear()
        }

        var numCardsToStock = shopSizes[tier] - stock.size

        // This should never happen, but fail fast in case game data is bad!
        require(possibleNewStock.values.sumOf { it.size } >= numCardsToStock)

        // Don't include frequency distribution entries for tiers we don't yet support
        val frequencyBuckets = FrequencyBuckets(frequencyDistribution.take(tier + 1))
        while (numCardsToStock > 0) {
            val tier = frequencyBuckets.pickRandomBucket(random)
            // Choices for a tier might be unavailable even if we wanted to randomly pick it, due to the passed in
            // filter. At that point, just give up and keep trying until we get a tier that works
            val tierChoices = possibleNewStock[tier]?.takeIf { cards -> cards.isNotEmpty() } ?: continue
            stock.add(tierChoices.removeAt(random.nextInt(tierChoices.size)).instantiate())
            numCardsToStock--
        }

        return true
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

    fun upgrade(): Boolean {
        if (tier >= shopSizes.size - 1) return false

        ++tier
        // New slot should ALWAYS contain a card from the new tier
        restock(restockAll = false) { card -> card.tier == tier }
        return true
    }

    fun copy() = MutableShop(random, allCards, shopSizes, frequencyDistribution, tier, stock.toMutableList())
}
