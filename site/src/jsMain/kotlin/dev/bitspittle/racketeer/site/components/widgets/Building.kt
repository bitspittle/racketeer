package dev.bitspittle.racketeer.site.components.widgets

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.graphics.Color
import dev.bitspittle.racketeer.model.building.*
import dev.bitspittle.racketeer.model.card.TraitType
import dev.bitspittle.racketeer.model.card.UpgradeType
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.text.Describer
import dev.bitspittle.racketeer.site.model.GameContext
import org.jetbrains.compose.web.css.*

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
            append(describer.describeInfluence(self.luck))
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
        enabled = !isActivated && state.canActivate(this)
                && state.cash >= self.blueprint.activationCost.cash
                && state.influence >= self.blueprint.activationCost.influence
                && state.luck >= self.blueprint.activationCost.luck
    )
}

fun Building.toCardSpec(describer: Describer, enabled: Boolean = true): CardSpec {
    val self = this
    return object : CardSpec {
        override val enabled = enabled
        override val colorOverride: Color? = null
        override val title = blueprint.name
        override val types = emptyList<String>()
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
        override val label = self.blueprint.activationCost.toLabel(describer)
    }
}

@Composable
fun Building(ctx: GameContext, building: Building, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(ctx.describer, ctx.tooltipParser, building.toCardSpec(ctx.describer, ctx.state), onClick, modifier)
}

fun Blueprint.toCardSpec(describer: Describer, state: GameState): CardSpec {
    return this.toCardSpec(
        describer,
        enabled = state.cash >= buildCost.cash && state.influence >= buildCost.influence
    )
}

fun Blueprint.toCardSpec(describer: Describer, enabled: Boolean = true): CardSpec {
    val self = this
    return object : CardSpec {
        override val enabled = enabled
        override val colorOverride: Color = Colors.LightBlue
        override val title = self.name
        override val types = emptyList<String>()
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
        override val label = self.buildCost.toLabel(describer)
    }
}


@Composable
fun Blueprint(ctx: GameContext, blueprint: Blueprint, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(ctx.describer, ctx.tooltipParser, blueprint.toCardSpec(ctx.describer, ctx.state), onClick, modifier)
}
