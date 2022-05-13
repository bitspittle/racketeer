package dev.bitspittle.racketeer.console.command.commands.buildings

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.views.game.buildings.BrowseBuildingsView

class BrowseBuildingsCommand(ctx: GameContext) : Command(ctx) {
    override val type = if (ctx.state.buildings.isNotEmpty()) Type.Normal else Type.Disabled
    override val title = "Browse buildings (${ctx.state.buildings.size})"

    override val description = "Look over your buildings."

    override suspend fun invoke(): Boolean {
        ctx.viewStack.pushView(BrowseBuildingsView(ctx))
        return true
    }
}

