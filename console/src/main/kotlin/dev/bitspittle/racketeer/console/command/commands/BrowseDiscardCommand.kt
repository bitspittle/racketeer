package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.views.BrowseDeckView
import dev.bitspittle.racketeer.console.view.views.BrowseShopView

class BrowseDiscardCommand(ctx: GameContext) : Command(ctx) {
    override val title = "Browse discard"

    override val description = "Look over the cards in the discard pile."

    override fun invoke() {
        ctx.viewStack.pushView(BrowseDeckView(ctx))
    }
}

