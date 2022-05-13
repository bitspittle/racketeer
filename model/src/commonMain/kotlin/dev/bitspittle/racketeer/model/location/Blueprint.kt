package dev.bitspittle.racketeer.model.location

import kotlinx.serialization.Serializable

@Serializable
class BuildCost(val cash: Int, val influence: Int)

@Serializable
class ActivationCost(val cash: Int = 0, val influence: Int = 0, val luck: Int = 0) {
    fun isFree() = cash == 0 && influence == 0 && luck == 0
}

/**
 * @param initActions Actions run when this blueprint is first built as a concrete location
 * @param canActivate An expression which must evaluate to true or false, checked before every user action. If false,
 *   the card won't show up as an option that can be activated. Note: If the card's [activationCost] can't be met, then
 *   the card will be considered ineligible for activation even if this expression returns true.
 */
@Serializable
data class Blueprint(
    val name: String,
    val flavor: String,
    val buildCost: BuildCost,
    val activationCost: ActivationCost = ActivationCost(),
    val rarity: Int = 0,
    val vp: Int = 0,
    val passiveVp: String? = null,
    val initActions: List<String> = emptyList(),
    val passiveActions: List<String> = emptyList(),
    val canActivate: String = "true",
    val activateActions: List<String> = emptyList(),
): Comparable<Blueprint> {
    fun build() = MutableLocation(this)

    override fun compareTo(other: Blueprint): Int = this.name.compareTo(other.name)
}

/**
 * A collection of all passive actions, both those written explicitly and also implicitly extracted from other fields
 */
val Blueprint.allPassiveActions: List<String> get() =
    (passiveVp?.let { expr -> listOf("location-set! \$this 'vp-passive ($expr)") } ?: emptyList()) + passiveActions
