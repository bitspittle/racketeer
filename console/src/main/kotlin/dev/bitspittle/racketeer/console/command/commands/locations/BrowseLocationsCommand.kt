package dev.bitspittle.racketeer.console.command.commands.locations

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.views.game.locations.BrowseLocationsView

class BrowseLocationsCommand(ctx: GameContext) : Command(ctx) {
    override val type = if (ctx.state.locations.isNotEmpty()) Type.Normal else Type.Disabled
    override val title = "Browse buildings (${ctx.state.locations.size})"

    override val description = "Look over your buildings."

    override suspend fun invoke(): Boolean {
        ctx.viewStack.pushView(BrowseLocationsView(ctx))
        return true
    }
}

