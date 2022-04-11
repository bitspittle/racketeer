package dev.bitspittle.racketeer.model.card

import kotlinx.serialization.Serializable

/**
 * @param cost How much this card costs to purchase. If a card has no cost, it means it [isPriceless], and it can't be
 *   sold in a shop.
 * @param vp How many active VP points this card has earned over its lifetime.
 * @param passiveVp An expression to calculate a bonus number of VP points based on some dynamic calculation
 *   (updated every time an action happens that affects the state of the board).
 */
@Serializable
data class CardTemplate(
    val name: String,
    val flavor: String,
    val types: List<String>,
    val tier: Int,
    val level: Int = 0,
    val vp: Int = 0,
    val passiveVp: String? = null,
    val cost: Int = 0,
    val initActions: List<String> = listOf(),
    val playActions: List<String> = listOf(),
    val passiveActions: List<String> = listOf(),
): Comparable<CardTemplate> {
    val isPriceless get() = cost == 0

    fun instantiate() = Card(this)

    override fun compareTo(other: CardTemplate): Int = this.name.compareTo(other.name)
}

/**
 * A collection of all passive actions, both those written explicitly and also implicitly extracted from other fields
 */
val CardTemplate.allPassiveActions: List<String> get() =
    (passiveVp?.let { expr -> listOf("card-set! \$this 'vp-passive ($expr)") } ?: listOf()) + passiveActions
