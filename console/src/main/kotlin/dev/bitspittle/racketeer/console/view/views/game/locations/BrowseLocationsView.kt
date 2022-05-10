package dev.bitspittle.racketeer.console.view.views.game.locations

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.locations.BuildBlueprintCommand
import dev.bitspittle.racketeer.console.command.commands.locations.ViewLocationCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.views.game.GameView

class BrowseLocationsView(ctx: GameContext) : GameView(ctx) {
    override val subtitle get() = "Locations"

    override fun createCommands(): List<Command> = run {
        ctx.state.locations.map { location -> ViewLocationCommand(ctx, location) }
    }
}