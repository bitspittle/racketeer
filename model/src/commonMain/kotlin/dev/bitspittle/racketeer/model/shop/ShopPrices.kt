@file:Suppress("MemberVisibilityCanBePrivate") // Exposed for serialization

package dev.bitspittle.racketeer.model.shop

import kotlinx.serialization.Serializable

@Serializable
class ShopPrices(
    val tier2: Int,
    val tier3: Int,
    val tier4: Int,
    val tier5: Int,
) {
    private val priceArray = arrayOf(0, tier2, tier3, tier4, tier5)
    operator fun get(tier: Int): Int = priceArray[tier]
}