package dev.bitspittle.racketeer.console.command.commands.game

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.views.game.BrowseShopView

class BrowseShopCommand(ctx: GameContext) : Command(ctx) {
    override val title = "Browse shop (Tier ${ctx.state.shop.tier + 1})"

    override val description = "Look over the cards in the shop, or take other relevant actions."

    override suspend fun invoke(): Boolean {
        ctx.viewStack.pushView(BrowseShopView(ctx))
        return true
    }
}

