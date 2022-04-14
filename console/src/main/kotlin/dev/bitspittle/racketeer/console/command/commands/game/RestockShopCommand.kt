package dev.bitspittle.racketeer.console.command.commands.game

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command

class RestockShopCommand(ctx: GameContext, padding: Int) : Command(ctx) {
    override val type = if (ctx.state.luck >= 1) Type.Modify else Type.Disabled

    override val title = "${"Restock shop".padEnd(padding)} ${ctx.describer.describeLuck(1)}"

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
