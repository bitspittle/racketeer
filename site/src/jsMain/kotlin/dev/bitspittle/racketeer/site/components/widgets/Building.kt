package dev.bitspittle.racketeer.site.components.widgets

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.ui.Modifier
import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.building.Building
import dev.bitspittle.racketeer.model.building.vpTotal
import dev.bitspittle.racketeer.model.card.UpgradeType
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.site.model.GameContext

fun Building.toCardSpec(state: GameState): CardSpec = this.toCardSpec(!isActivated && state.canActivate(this))
fun Building.toCardSpec(enabled: Boolean = true): CardSpec {
    val self = this
    return object : CardSpec {
        override val title = blueprint.name
        override val types = emptyList<String>()
        override val tier = null
        override val rarity = blueprint.rarity
        override val vpBase = blueprint.vp
        override val vpTotal = self.vpTotal
        override val counter = self.counter
        override val flavor = blueprint.description.flavor
        override val upgrades = emptySet<UpgradeType>()
        override val ability = blueprint.description.ability
        override val enabled = enabled
    }
}

@Composable
fun Building(ctx: GameContext, building: Building, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(ctx.describer, building.toCardSpec(ctx.state), onClick, modifier)
}

fun Blueprint.toCardSpec(state: GameState) =
    this.toCardSpec(enabled = state.cash >= buildCost.cash && state.influence >= buildCost.influence)

fun Blueprint.toCardSpec(enabled: Boolean = true): CardSpec {
    val self = this
    return object : CardSpec {
        override val title = self.name
        override val types = emptyList<String>()
        override val tier = null
        override val rarity = self.rarity
        override val vpBase = self.vp
        override val vpTotal = null
        override val counter = 0
        override val flavor = self.description.flavor
        override val upgrades = emptySet<UpgradeType>()
        override val ability = self.description.ability
        override val enabled = enabled
    }
}


@Composable
fun Blueprint(ctx: GameContext, blueprint: Blueprint, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(ctx.describer, blueprint.toCardSpec(ctx.state), onClick, modifier)
}
