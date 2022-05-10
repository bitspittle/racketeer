package dev.bitspittle.racketeer.console.view.views.game.locations

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.locations.BuildBlueprintCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.views.game.GameView

class BrowseBlueprintsView(ctx: GameContext) : GameView(ctx) {
    override val subtitle get() = "Blueprints"

    override fun createCommands(): List<Command> = run {
        ctx.state.blueprints.map { blueprint -> BuildBlueprintCommand(ctx, blueprint) }
    }
}