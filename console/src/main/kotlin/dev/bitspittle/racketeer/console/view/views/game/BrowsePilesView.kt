package dev.bitspittle.racketeer.console.view.views.game

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.*
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.model.game.isGameOver

class BrowsePilesView(ctx: GameContext) : GameView(ctx) {
    override fun createCommands() = (if (!ctx.state.isGameOver) {
        listOf(BrowseShopCommand(ctx))
    } else emptyList<Command>()) + listOf(
        BrowseStreetCommand(ctx),
        BrowseHandCommand(ctx),
        BrowseDeckCommand(ctx),
        BrowseDiscardCommand(ctx),
        BrowseJailCommand(ctx),
        BrowseOwnedCardsCommand(ctx),
    )
}