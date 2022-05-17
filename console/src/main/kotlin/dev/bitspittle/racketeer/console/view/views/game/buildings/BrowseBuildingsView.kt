package dev.bitspittle.racketeer.console.view.views.game.buildings

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.buildings.ViewBuildingCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View

class BrowseBuildingsView(ctx: GameContext) : View(ctx) {
    override val subtitle get() = "Buildings"

    override fun createCommands(): List<Command> = run {
        ctx.state.buildings.map { building -> ViewBuildingCommand(ctx, building) }
    }
}