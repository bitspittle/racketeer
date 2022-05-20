package dev.bitspittle.racketeer.console.view.views.game.play

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.play.DrawCardsCommand
import dev.bitspittle.racketeer.console.command.commands.game.play.VisitShopCommand
import dev.bitspittle.racketeer.console.command.commands.buildings.VisitBlueprintsCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View

class PreDrawView(ctx: GameContext) : View(ctx) {
    override fun createCommands(): List<Command> = listOf(
        DrawCardsCommand(ctx),
        VisitShopCommand(ctx),
        VisitBlueprintsCommand(ctx),
    )
}