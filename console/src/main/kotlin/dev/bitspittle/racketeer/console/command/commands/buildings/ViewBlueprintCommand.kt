package dev.bitspittle.racketeer.console.command.commands.buildings

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.model.building.Blueprint

fun Blueprint.shouldMask(ctx: GameContext) = !ctx.userStats.buildings.contains(this.name)

/**
 * A no-op command used when read-only viewing a blueprint
 */
class ViewBlueprintCommand(ctx: GameContext, blueprint: Blueprint) : Command(ctx) {
    private val shouldMaskCard = blueprint.shouldMask(ctx)

    override val type = if (shouldMaskCard) Type.Disabled else Type.Normal
    override val title = if (shouldMaskCard) "?".repeat(blueprint.name.length) else ctx.describer.describeBlueprint(blueprint, concise = true)
    override val description = if (shouldMaskCard) "You must build this building at least once to see its details." else ctx.describer.describeBlueprint(blueprint, concise = false)
}
