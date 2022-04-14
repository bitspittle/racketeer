package dev.bitspittle.racketeer.console.view.views.game

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.*
import dev.bitspittle.racketeer.console.view.View

class PreDrawView(ctx: GameContext) : View(ctx) {
    override fun createCommands(): List<Command> = listOf(
        DrawCardsCommand(ctx),
        BrowseHandCommand(ctx),
        BrowseShopCommand(ctx),
        BrowseDeckCommand(ctx),
        BrowseDiscardCommand(ctx),
        BrowseJailCommand(ctx),
    )
}