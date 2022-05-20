package dev.bitspittle.racketeer.console.view.views.game.buildings

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.buildings.BuildBlueprintCommand
import dev.bitspittle.racketeer.console.command.commands.game.play.VisitShopCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View

class VisitBlueprintsView(ctx: GameContext) : View(ctx) {
    override val subtitle get() = "Blueprints"

    override fun createCommands(): List<Command> =
        ctx.state.blueprints.map { blueprint -> BuildBlueprintCommand(ctx, blueprint) } +
                VisitShopCommand(ctx)

}