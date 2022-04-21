package dev.bitspittle.racketeer.console.view.views.game

import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.MainRenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.*
import dev.bitspittle.racketeer.console.game.GameContext

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