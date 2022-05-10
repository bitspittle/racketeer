package dev.bitspittle.racketeer.console.command.commands.locations

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.utils.runStateChangingAction
import dev.bitspittle.racketeer.model.game.GameProperty
import dev.bitspittle.racketeer.model.game.GameStateChange
import dev.bitspittle.racketeer.model.location.Blueprint
import dev.bitspittle.racketeer.model.location.Location

/**
 * A no-op command used when read-only viewing a blueprint
 */
class ViewLocationCommand(ctx: GameContext, location: Location) : Command(ctx) {
    override val title = ctx.describer.describeLocation(location, concise = true)
    override val description = ctx.describer.describeLocation(location, showActivated = true, concise = false)
}
