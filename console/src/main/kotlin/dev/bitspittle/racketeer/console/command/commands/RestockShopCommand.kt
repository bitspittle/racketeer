package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.views.BrowseShopView

class RestockShopCommand(ctx: GameContext) : Command(ctx) {
    override val type = if (ctx.state.luck >= 1) Type.Modify else Type.Disabled
    override val title = "Restock shop ${ctx.describer.describeLuck(1)}"

    override val description = "Refill the shop with random cards."

    override suspend fun invoke(): Boolean {
        return if (ctx.state.shop.restock()) {
            ctx.state.luck -= 1
            ctx.viewStack.currentView.refreshCommands()
            true
        } else {
            false
        }
    }
}
