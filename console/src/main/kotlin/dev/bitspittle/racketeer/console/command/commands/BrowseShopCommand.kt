package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.views.BrowseShopView

class BrowseShopCommand(ctx: GameContext) : Command(ctx) {
    override val title = "Browse shop (tier ${ctx.state.shopTier + 1})"

    override val description = "Look over the cards in the shop, or take other relevant actions."

    override suspend fun invoke(): Boolean {
        ctx.viewStack.pushView(BrowseShopView(ctx))
        return true
    }
}

