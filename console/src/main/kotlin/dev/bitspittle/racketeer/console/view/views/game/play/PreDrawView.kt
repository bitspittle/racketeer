package dev.bitspittle.racketeer.console.view.views.game.play

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.play.DrawCardsCommand
import dev.bitspittle.racketeer.console.command.commands.game.play.VisitShopCommand
import dev.bitspittle.racketeer.console.command.commands.buildings.BrowseBlueprintsCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.views.game.GameView

class PreDrawView(ctx: GameContext) : GameView(ctx) {
    override fun createCommands(): List<Command> = listOf(
        DrawCardsCommand(ctx),
        VisitShopCommand(ctx),
        BrowseBlueprintsCommand(ctx),
    )
}