package dev.bitspittle.racketeer.model.card

import dev.bitspittle.limp.utils.toIdentifierName
import dev.bitspittle.racketeer.model.game.Feature
import kotlinx.serialization.Serializable

/**
 * @param features Which feature expansions this card should be limited to. If empty, this card is considered part of
 *   the base set. If more than one feature is specified, ALL features have to be enabled for this to appear.
 * @param shopCount An override to how many maximum instances of this card can ever be sold (or stolen!) from the shop.
 *   See also [Rarity.shopCount].
 * @param cost How much this card costs to purchase. If a card has no cost, it means it [isPriceless], and it can't be
 *   sold in a shop.
 * @param vp How many active VP points this card has earned over its lifetime.
 * @param passiveVp An expression to calculate a bonus number of VP points based on some dynamic calculation
 *   (updated every time an action happens that affects the state of the board).
 */
@Serializable
data class CardTemplate(
    val name: String,
    val types: List<String>,
    val tier: Int,
    val description: Description,
    val features: List<String> = emptyList(),
    val rarity: Int = 0,
    val shopCount: Int? = null,
    val vp: Int = 0,
    val passiveVp: String? = null,
    val cost: Int = 0,
    val upgrades: List<String> = emptyList(),
    val initActions: List<String> = emptyList(),
    val playActions: List<String> = emptyList(),
    val passiveActions: List<String> = emptyList(),
): Comparable<CardTemplate> {
    /**
     * @param ability Text which describes the ability of what this card does. See also: [flavor]
     * @param flavor Text which adds color to the card but in no way affects its gameplay. See also: [ability]
     */
    @Serializable
    data class Description(
        val ability: String,
        val flavor: String? = null,
    )

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

val CardTemplate.featureTypes: Set<Feature.Type>
    get() = features.map { featureStr ->
        Feature.Type.values().first { it.toIdentifierName() == featureStr }
    }.toSet()

val CardTemplate.upgradeTypes: Set<UpgradeType> get() = upgrades.map { upgradeStr ->
    UpgradeType.values().first { it.toIdentifierName() == upgradeStr }
}.toSet()