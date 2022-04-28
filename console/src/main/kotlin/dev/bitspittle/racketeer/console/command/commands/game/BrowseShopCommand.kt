package dev.bitspittle.racketeer.console.command.commands.game

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.views.game.BrowseShopView

class BrowseShopCommand(ctx: GameContext) : Command(ctx) {
    override val type = if (ctx.state.shop.stock.any { it != null }) Type.Normal else Type.Disabled
    override val title = "Browse shop (${ctx.state.shop.stock.count { it != null }})"

    override val description = "Look over the cards in the shop."

    override suspend fun invoke(): Boolean {
        ctx.viewStack.pushView(BrowseShopView(ctx))
        return true
    }
}

