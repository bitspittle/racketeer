package dev.bitspittle.racketeer.model.card

import kotlinx.serialization.Serializable

/**
 * @param cost How much this card costs to purchase. If a card has no cost, it means it [isPriceless], and it can't be
 *   sold in a shop.
 * @param vp How many points this card is worth for VP as long as it is not thrown in jail at some point
 */
@Serializable
data class CardTemplate(
    val name: String,
    val flavor: String,
    val types: List<String>,
    val tier: Int = 0,
    val vp: Int = 0,
    val cost: Int = 0,
    val actions: List<String> = listOf(),
) {
    val isPriceless get() = cost == 0

    fun instantiate() = Card(this)
}
