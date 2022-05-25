package dev.bitspittle.racketeer.console.command.commands.buildings

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.model.building.Blueprint

fun Blueprint.shouldMask(ctx: GameContext) = !ctx.userStats.buildings.contains(this.name)

/**
 * A no-op command used when read-only viewing a blueprint
 */
class ViewBlueprintCommand(ctx: GameContext, blueprint: Blueprint, shouldMask: Boolean = blueprint.shouldMask(ctx)) : Command(ctx) {
    override val type = if (shouldMask) Type.Disabled else Type.Normal
    override val title = if (shouldMask) "?".repeat(blueprint.name.length) else ctx.describer.describeBlueprintTitle(blueprint)
    override val description = if (shouldMask) "You must build this building at least once to see its details." else ctx.describer.describeBlueprintBody(blueprint, includeFlavor = true)
}
