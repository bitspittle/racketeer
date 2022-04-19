package dev.bitspittle.racketeer.model.shop

import com.benasher44.uuid.Uuid
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate
import kotlin.random.Random

interface Shop {
    val tier: Int
    val stock: List<Card?>
    val exclusions: List<String>
    fun addExclusion(exclusion: Exclusion)
    suspend fun upgrade(): Boolean
    suspend fun restock(restockAll: Boolean = true, additionalFilter: suspend (CardTemplate) -> Boolean = { true }): Boolean
}

class MutableShop private constructor(
    private val random: Random,
    private val allCards: List<CardTemplate>,
    private val shopSizes: List<Int>,
    private val tierFrequencies: List<Int>,
    private val rarityFrequencies: List<Int>,
    tier: Int,
    override val stock: MutableList<Card?>,
    private val _exclusions: MutableList<Exclusion>,
) : Shop {
    constructor(random: Random, allCards: List<CardTemplate>, shopSizes: List<Int>, tierFrequencies: List<Int>, rarityFrequencies: List<Int>) : this(
        random,
        allCards,
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

    override val exclusions get() = _exclusions.map { it.desc }

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
            .filter { card -> card.cost > 0 && card.tier <= this.tier && additionalFilter(card) }
            .groupByTo(mutableMapOf()) { it.tier }
    }

    override suspend fun restock(restockAll: Boolean, additionalFilter: suspend (CardTemplate) -> Boolean): Boolean {
        return handleRestock(
            restockAll,
            filterAllCards { card -> additionalFilter(card) && _exclusions.none { exclude -> exclude(card) } })
    }

    override fun addExclusion(exclusion: Exclusion) { _exclusions.add(exclusion) }

    fun remove(cardId: Uuid) {
        for (i in stock.indices) {
            stock[i]?.run {
                if (this.id == cardId) {
                    stock[i] = null
                    return
            }}
        }
    }

    override suspend fun upgrade(): Boolean {
        if (tier >= shopSizes.size - 1) return false

        ++tier
        // New slot should ALWAYS contain a card from the new tier
        restock(restockAll = false) { card -> card.tier == tier }
        return true
    }

    fun copy() = MutableShop(
        random,
        allCards,
        shopSizes,
        tierFrequencies,
        rarityFrequencies,
        tier,
        stock.toMutableList(),
        _exclusions.toMutableList()
    )
}
