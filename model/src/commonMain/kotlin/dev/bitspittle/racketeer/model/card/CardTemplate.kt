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
    val rarity: Int = 0,
    val vp: Int = 0,
    val passiveVp: String? = null,
    val cost: Int = 0,
    val upgrades: List<String> = emptyList(),
    val initActions: List<String> = emptyList(),
    val playActions: List<String> = emptyList(),
    val passiveActions: List<String> = emptyList(),
): Comparable<CardTemplate> {
    val isPriceless get() = cost == 0

    fun instantiate() = MutableCard(this)

    override fun compareTo(other: CardTemplate): Int = this.name.compareTo(other.name)
}

/**
 * A collection of all passive actions, both those written explicitly and also implicitly extracted from other fields
 */
val CardTemplate.allPassiveActions: List<String> get() =
    (passiveVp?.let { expr -> listOf("card-set! \$this 'vp-passive ($expr)") } ?: emptyList()) + passiveActions

/**
 * A collection of all init actions, both those written explicitly and also implicitly extracted from other fields
 */
val CardTemplate.allInitActions: List<String> get() =
    (if (upgradeTypes.contains(UpgradeType.SWIFT)) listOf("pile-move-to! \$hand \$this") else emptyList()) + initActions

val CardTemplate.upgradeTypes: Set<UpgradeType> get() = upgrades.map { upgradeStr ->
    UpgradeType.values().first { it.name.compareTo(upgradeStr, ignoreCase = true) == 0 }
}.toSet()