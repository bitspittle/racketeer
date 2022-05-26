package dev.bitspittle.racketeer.model.building

import dev.bitspittle.racketeer.model.game.GameState
import kotlinx.serialization.Serializable

@Serializable
class BuildCost(val cash: Int, val influence: Int)

@Serializable
class ActivationCost(val cash: Int = 0, val influence: Int = 0, val luck: Int = 0) {
    fun isFree() = cash == 0 && influence == 0 && luck == 0
}

/**
 * @param initActions Actions run when this blueprint is first built as a concrete building
 * @param canActivate An expression which must evaluate to true or false, checked before every user action. If false,
 *   the card won't show up as an option that can be activated. Note: If the card's [activationCost] can't be met, then
 *   the card will be considered ineligible for activation even if this expression returns true.
 * @param cannotActivateReason A user visible reason for why this building could not activate, which should essentially
 *   describe what the [canActivate] method is checking for. If it is not set and [canActivate] evaluates to false,
 *   then the building won't be shown to the user at all on the main game view. Otherwise, it will show up disabled and
 *   display text like, "This building could not be activated because (reason)."
 */
@Serializable
data class Blueprint(
    val name: String,
    val description: Description,
    val buildCost: BuildCost,
    val activationCost: ActivationCost = ActivationCost(),
    val rarity: Int,
    val vp: Int = 0,
    val passiveVp: String? = null,
    val initActions: List<String> = emptyList(),
    val passiveActions: List<String> = emptyList(),
    val canActivate: String = "true",
    val cannotActivateReason: String? = null,
    val activateActions: List<String> = emptyList(),
): Comparable<Blueprint> {
    /**
     * @param ability Text which describes the ability of what this card does. See also: [flavor]
     * @param flavor Text which adds color to the card but in no way affects its gameplay. See also: [ability]
     */
    @Serializable
    data class Description(
        val ability: String,
        val flavor: String? = null,
    )

    fun build() = MutableBuilding(this)

    override fun compareTo(other: Blueprint): Int = this.name.compareTo(other.name)
}

/**
 * A collection of all passive actions, both those written explicitly and also implicitly extracted from other fields
 */
val Blueprint.allPassiveActions: List<String> get() =
    (passiveVp?.let { expr -> listOf("building-set! \$this 'vp-passive ($expr)") } ?: emptyList()) + passiveActions

fun Blueprint.isBuilt(state: GameState) = state.buildings.any { it.blueprint === this }
fun Blueprint.isOwned(state: GameState) = state.blueprints.contains(this) || isBuilt(state)