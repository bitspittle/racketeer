package dev.bitspittle.racketeer.console.command.commands.buildings

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.views.game.buildings.BrowseBlueprintsView

class BrowseBlueprintsCommand(ctx: GameContext) : Command(ctx) {
    override val type = if (ctx.state.blueprints.isNotEmpty()) Type.Normal else Type.Hidden
    override val title = "Browse blueprints (${ctx.state.blueprints.size})"

    override val description = "Look over your blueprints."

    override suspend fun invoke(): Boolean {
        ctx.viewStack.pushView(BrowseBlueprintsView(ctx))
        return true
    }
}

