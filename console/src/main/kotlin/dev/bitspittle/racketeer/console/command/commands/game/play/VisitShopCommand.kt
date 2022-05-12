package dev.bitspittle.racketeer.console.command.commands.game.play

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.views.game.shop.VisitShopView
import dev.bitspittle.racketeer.model.shop.Shop

fun Shop.canAffordSomething(ctx: GameContext): Boolean {
    return (
            ctx.state.cash >= (stock.filterNotNull().minOfOrNull { it.template.cost } ?: Int.MAX_VALUE) ||
                    (ctx.state.shop.tier < ctx.data.maxTier && ctx.state.influence >= ctx.data.shopPrices[ctx.state.shop.tier])
            )
}

class VisitShopCommand(ctx: GameContext) : Command(ctx) {
    override val type = if (ctx.state.shop.canAffordSomething(ctx)) Type.Emphasized else Type.Normal

    override val title = "Visit shop (Tier ${ctx.state.shop.tier + 1})"

    override val description = "Look over the cards in the shop, or take other relevant actions."

    override suspend fun invoke(): Boolean {
        ctx.viewStack.pushView(VisitShopView(ctx))
        return true
    }
}

