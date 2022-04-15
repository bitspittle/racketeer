package dev.bitspittle.racketeer.console.view.views.game

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.*
import dev.bitspittle.racketeer.console.view.View

class BrowseShopView(ctx: GameContext) : View(ctx) {
    override val subtitle = "Shop"

    override fun createCommands(): List<Command> = run {
        val maxNameLen =
            ctx.state.shop.stock.filterNotNull().maxOfOrNull { ctx.describer.describe(it, concise = true).length } ?: 0

        val maxTitleLen = maxOf(
            maxNameLen + "Buy: ".length,
            "Restock shop".length,
            "Expand shop".length
        )


        ctx.state.shop.stock.map { card ->
            if (card != null) BuyCardCommand(ctx, card, maxTitleLen) else SoldOutCardCommand(ctx)
        } + listOf(
            RestockShopCommand(ctx, maxTitleLen),
            if (ctx.state.shop.tier < ctx.data.maxTier) ExpandShopCommand(ctx, maxTitleLen) else MaxedOutShopCommand(
                ctx,
                maxTitleLen
            )
        )
    }
}