package dev.bitspittle.racketeer.site.components.widgets

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.ui.Modifier
import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.building.Building
import dev.bitspittle.racketeer.model.building.vpTotal
import dev.bitspittle.racketeer.site.model.GameContext

@Composable
fun Building(ctx: GameContext, building: Building, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        ctx,
        object : CardSpec {
            override val title = building.blueprint.name
            override val vpBase = building.blueprint.vp
            override val vpTotal = building.vpTotal
            override val flavor = building.blueprint.description.flavor
            override val ability = building.blueprint.description.ability
            override val enabled = !building.isActivated && ctx.state.canActivate(building)
        },
        onClick, modifier
    )
}

@Composable
fun Blueprint(ctx: GameContext, blueprint: Blueprint, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        ctx,
        object : CardSpec {
            override val title = blueprint.name
            override val vpBase = blueprint.vp
            override val vpTotal = null
            override val flavor = blueprint.description.flavor
            override val ability = blueprint.description.ability
            override val enabled = ctx.state.cash >= blueprint.buildCost.cash && ctx.state.influence >= blueprint.buildCost.influence
        },
        onClick, modifier
    )
}
