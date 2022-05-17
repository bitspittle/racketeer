package dev.bitspittle.racketeer.console.command.commands.buildings

import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.model.building.Building

/**
 * A no-op command used when read-only viewing a building
 */
class ViewBuildingCommand(ctx: GameContext, private val building: Building) : Command(ctx) {
    private val canActivate = ctx.state.canActivate(building)
    override val type: Type = when {
        !building.isActivated && canActivate && building.canAffordActivationCost(ctx.state) -> Type.Normal
        else -> Type.Disabled
    }
    override val title = ctx.describer.describeBuilding(building, concise = true)
    override val description = ctx.describer.describeBuilding(building, showActivatedState = true, concise = false)

    override fun renderContentLowerInto(scope: RenderScope) {
        building.renderCannotActivateReason(ctx.describer, ctx.state, scope)
    }
}
