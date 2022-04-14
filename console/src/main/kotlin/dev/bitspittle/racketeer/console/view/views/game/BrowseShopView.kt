package dev.bitspittle.racketeer.console.view.views.game

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.*
import dev.bitspittle.racketeer.console.view.View

class BrowseShopView(ctx: GameContext) : View(ctx) {
    override val title = "Shop"

    override fun createCommands(): List<Command> = run {
        val maxNameLen =
            ctx.state.shop.stock.filterNotNull().maxOf { ctx.describer.describe(it, concise = true).length }
        val maxBuyLen = "Buy: ".length + maxNameLen
        ctx.state.shop.stock.map { card ->
            if (card != null) BuyCardCommand(ctx, card, maxNameLen) else SoldOutCardCommand(ctx)
        } + listOf(
            RestockShopCommand(ctx, maxBuyLen),
            if (ctx.state.shop.tier < ctx.data.maxTier) ExpandShopCommand(ctx, maxBuyLen) else MaxedOutShopCommand(
                ctx,
                maxBuyLen
            )
        )
    }
}