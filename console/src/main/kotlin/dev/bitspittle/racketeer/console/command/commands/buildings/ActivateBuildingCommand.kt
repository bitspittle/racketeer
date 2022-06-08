package dev.bitspittle.racketeer.console.command.commands.buildings

import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.utils.runStateChangingAction
import dev.bitspittle.racketeer.model.game.GameProperty
import dev.bitspittle.racketeer.model.game.GameStateChange
import dev.bitspittle.racketeer.model.building.Building

class ActivateBuildingCommand(ctx: GameContext, private val building: Building) : Command(ctx) {
    private val canActivate = ctx.state.canActivate(building)

    override val type = when {
        building.isActivated -> Type.Hidden
        !canActivate && building.blueprint.cannotActivateReason == null -> Type.Hidden
        canActivate && building.canAffordActivationCost(ctx.state) -> Type.Emphasized
        else -> Type.Disabled
    }

    override val title = "Activate: " + ctx.describer.describeBuildingTitle(building)
    override val description = ctx.describer.describeBuildingBody(building)
    override val extra: String = ctx.describer.describeActivationCost(building.blueprint)

    override fun renderContentLowerInto(scope: RenderScope) {
        building.renderCannotActivateReason(ctx.describer, ctx.state, scope)
    }

    override suspend fun invoke(): Boolean {
        ctx.runStateChangingAction {
            ctx.state.apply(GameStateChange.Activate(building))
        }
        return true
    }
}
