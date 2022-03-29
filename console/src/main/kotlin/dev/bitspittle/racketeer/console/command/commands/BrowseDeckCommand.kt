package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.views.BrowseDeckView
import dev.bitspittle.racketeer.console.view.views.BrowseShopView

class BrowseDeckCommand(ctx: GameContext) : Command(ctx) {
    override val title = "Browse deck"

    override val description = "Look over the cards in your deck."

    override fun invoke() {
        ctx.viewStack.pushView(BrowseDeckView(ctx))
    }
}

