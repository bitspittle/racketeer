package dev.bitspittle.racketeer.console.view.views

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.BrowseDeckCommand
import dev.bitspittle.racketeer.console.command.commands.BrowseDiscardCommand
import dev.bitspittle.racketeer.console.command.commands.BrowseShopCommand
import dev.bitspittle.racketeer.console.command.commands.DrawCardsCommand
import dev.bitspittle.racketeer.console.view.View

class PreDrawView(ctx: GameContext) : View(ctx) {
    override fun createCommands(): List<Command> = listOf(
        DrawCardsCommand(ctx),
        BrowseShopCommand(ctx),
        BrowseDeckCommand(ctx),
        BrowseDiscardCommand(ctx),
    )
}