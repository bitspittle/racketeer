package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.views.BrowseShopView

class BrowseShopCommand(private val ctx: GameContext) : Command {
    override val title = "Browse shop (Tier ${ctx.state.shopTier + 1})"

    override val description = "Look over the cards in the shop, or take other relevant actions."

    override fun invoke() {
        ctx.viewStack.pushView(BrowseShopView(ctx))
    }
}

