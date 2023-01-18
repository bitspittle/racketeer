package dev.bitspittle.racketeer.site.components.widgets

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.dom.ElementTarget
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.silk.components.overlay.Tooltip
import dev.bitspittle.racketeer.model.building.*
import dev.bitspittle.racketeer.model.card.TraitType
import dev.bitspittle.racketeer.model.card.UpgradeType
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.text.Describer
import dev.bitspittle.racketeer.site.model.GameContext
import dev.bitspittle.racketeer.site.model.user.UserData

private fun Blueprint.canAffordBuildCost(state: GameState) =
    state.cash >= this.buildCost.cash && state.influence >= this.buildCost.influence

private fun Building.canAffordActivationCost(state: GameState) =
    state.cash >= blueprint.activationCost.cash &&
            state.influence >= blueprint.activationCost.influence &&
            state.luck >= blueprint.activationCost.luck


private fun ActivationCost.toLabel(describer: Describer): String? {
    val self = this
    return buildString {
        if (self.cash > 0) {
            append(describer.describeCash(self.cash))
        }
        if (self.influence > 0) {
            if (this.isNotEmpty()) append(' ')
            append(describer.describeInfluence(self.influence))
        }
        if (self.luck > 0) {
            if (this.isNotEmpty()) append(' ')
            append(describer.describeLuck(self.luck))
        }
    }.takeIf { it.isNotEmpty() }
}

private fun BuildCost.toLabel(describer: Describer): String {
    val self = this
    return buildString {
        if (self.cash > 0) {
            append(describer.describeCash(self.cash))
        }
        if (self.influence > 0) {
            if (this.isNotEmpty()) append(' ')
            append(describer.describeInfluence(self.influence))
        }
    }
}

fun Building.toCardSpec(describer: Describer, state: GameState): CardSpec {
    val self = this
    return this.toCardSpec(
        describer,
        enabled = !isActivated && state.canActivate(this) && self.canAffordActivationCost(state)
    )
}

fun Building.toCardSpec(describer: Describer, enabled: Boolean = true): CardSpec {
    val self = this
    return object : CardSpec {
        override val enabled = enabled
        override val colorOverride: Color? = null
        override val title = blueprint.name
        override val types = listOf("Building")
        override val tier = null
        override val rarity = blueprint.rarity
        override val vpBase = blueprint.vp
        override val vpTotal = self.vpTotal
        override val counter = self.counter
        override val flavor = blueprint.description.flavor
        override val upgrades = emptySet<UpgradeType>()
        override val traits = emptySet<TraitType>()
        override val ability = blueprint.description.ability
        override val activationCost = null
        override val newTarget: NewIndicatorTarget? = null
        override val label = self.blueprint.activationCost.toLabel(describer)
    }
}

@Composable
fun Building(ctx: GameContext, building: Building, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(ctx.data, ctx.userStats, ctx.describer, ctx.tooltipParser, building.toCardSpec(ctx.describer, ctx.state), onClick, modifier)
    val msg = if (!building.isActivated && building.canAffordActivationCost(ctx.state) && !ctx.state.canActivate(building) && building.blueprint.cannotActivateReason != null) {
        ctx.describer.convertIcons(building.blueprint.cannotActivateReason!!)
    } else if (building.isActivated) {
        "you already activated it this turn"
    } else {
        null
    }

    if (msg != null) {
        Tooltip(ElementTarget.PreviousSibling, "This building cannot be activated because\n$msg.")
    }
}

fun Blueprint.toCardSpec(userStats: UserData.Stats, describer: Describer, state: GameState): CardSpec {
    val self = this
    return this.toCardSpec(
        userStats,
        describer,
        enabled = self.canAffordBuildCost(state)
    )
}

fun Blueprint.toCardSpec(userStats: UserData.Stats, describer: Describer, enabled: Boolean = true): CardSpec {
    val self = this
    return object : CardSpec {
        override val enabled = enabled
        override val colorOverride: Color = Colors.LightBlue
        override val title = self.name
        override val types = listOf("Blueprint")
        override val tier = null
        override val rarity = self.rarity
        override val vpBase = self.vp
        override val vpTotal = null
        override val counter = 0
        override val flavor = self.description.flavor
        override val upgrades = emptySet<UpgradeType>()
        override val traits = emptySet<TraitType>()
        override val ability = self.description.ability
        override val activationCost = self.activationCost.toLabel(describer)
        override val newTarget: NewIndicatorTarget? = NewIndicatorTarget.BLUEPRINT.takeUnless { userStats.buildings.totalBuilt > 0 }
        override val label = self.buildCost.toLabel(describer)
    }
}


@Composable
fun Blueprint(ctx: GameContext, blueprint: Blueprint, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        ctx.data,
        ctx.userStats,
        ctx.describer,
        ctx.tooltipParser,
        blueprint.toCardSpec(ctx.userStats, ctx.describer, ctx.state),
        onClick,
        modifier)
}
