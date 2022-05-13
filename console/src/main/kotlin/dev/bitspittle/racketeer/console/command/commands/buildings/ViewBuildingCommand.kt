package dev.bitspittle.racketeer.console.command.commands.buildings

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.model.building.Building

/**
 * A no-op command used when read-only viewing a building
 */
class ViewBuildingCommand(ctx: GameContext, building: Building) : Command(ctx) {
    override val type: Type = when {
        // TODO: Handle canActivate == false
        building.isActivated -> Type.Disabled
        else -> Type.Normal
    }
    override val title = ctx.describer.describeBuilding(building, concise = true)
    override val description = ctx.describer.describeBuilding(building, showActivated = true, concise = false)
}
