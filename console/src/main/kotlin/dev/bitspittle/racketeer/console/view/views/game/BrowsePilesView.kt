package dev.bitspittle.racketeer.console.view.views.game

import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.MainRenderScope
import dev.bitspittle.racketeer.console.command.commands.game.*
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View

class BrowsePilesView(ctx: GameContext) : View(ctx) {
    override fun createCommands() = listOf(
        BrowseShopCommand(ctx),
        BrowseStreetCommand(ctx),
        BrowseHandCommand(ctx),
        BrowseDeckCommand(ctx),
        BrowseDiscardCommand(ctx),
        BrowseJailCommand(ctx),
    )

    override fun MainRenderScope.renderContentUpper() {
        textLine("You own ${ctx.state.getOwnedCards().size} cards.")
        textLine()
    }
}