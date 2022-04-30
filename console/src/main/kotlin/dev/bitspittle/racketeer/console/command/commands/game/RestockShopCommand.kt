package dev.bitspittle.racketeer.console.command.commands.game

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.utils.runStateChangingAction

class RestockShopCommand(ctx: GameContext) : Command(ctx) {
    override val type = if (ctx.state.luck >= 1) Type.Emphasized else Type.Disabled

    override val title = "Restock shop"
    override val extra = ctx.describer.describeLuck(1)

    override val description = "Refill the shop with random cards."

    override suspend fun invoke(): Boolean {
        ctx.runStateChangingAction {
            ctx.state.shop.restock()
            ctx.state.luck -= 1
        }
        return true
    }
}
