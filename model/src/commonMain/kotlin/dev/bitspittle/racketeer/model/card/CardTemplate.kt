package dev.bitspittle.racketeer.model.card

import kotlinx.serialization.Serializable

/**
 * @param cost How much this card costs to purchase. If a card is [Cost.priceless], it can't be sold in a shop.
 * @param vp How many points this card is worth for VP as long as it is not thrown in jail at some point
 */
@Serializable
data class CardTemplate(
    val name: String,
    val flavor: String,
    val tier: Int = 0,
    val vp: Int = 0,
    val cost: Cost = Cost(),
    val actions: List<String> = listOf(),
) {
    @Serializable
    /**
     * @param cash How much this card costs in money
     * @param influence How much this card costs in influence points
     */
    data class Cost(
        val cash: Int = 0,
        val influence: Int = 0,
    ) {
        val priceless = cash == 0 && influence == 0
    }

    fun instantiate() = Card(this)
}

class Card internal constructor(val template: CardTemplate) {
    /**
     * Cards can earn victory points over the course of the game.
     */
    var vp = template.vp
        private set
}
