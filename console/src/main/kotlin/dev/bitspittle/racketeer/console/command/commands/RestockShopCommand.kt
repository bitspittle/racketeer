package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.views.BrowseShopView

class RestockShopCommand(ctx: GameContext) : Command(ctx) {
    override val title = "Restock shop ${ctx.describer.describeLuck(1)}"

    override val description = "Refill the shop with random cards."

    override suspend fun invoke(): Boolean {
        return if (ctx.state.luck >= 1 && ctx.state.shop.restock()) {
            ctx.state.luck -= 1
            ctx.viewStack.replaceView(BrowseShopView(ctx))
            true
        } else {
            false
        }
    }
}
