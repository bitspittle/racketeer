package dev.bitspittle.racketeer.console.command.commands.game.shop

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.utils.runStateChangingAction
import dev.bitspittle.racketeer.model.game.GameProperty
import dev.bitspittle.racketeer.model.game.GameStateChange

class RestockShopCommand(ctx: GameContext) : Command(ctx) {
    override val type = if (ctx.state.luck >= 1) Type.Emphasized else Type.Disabled

    override val title = "Restock shop"
    override val extra = ctx.describer.describeLuck(1)

    override val description = "Refill the shop with random cards."

    override suspend fun invoke(): Boolean {
        ctx.runStateChangingAction {
            ctx.state.addChange(GameStateChange.RestockShop())
            ctx.state.addChange(GameStateChange.AddGameAmount(GameProperty.LUCK, -1))
        }
        return true
    }
}
