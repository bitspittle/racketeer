package dev.bitspittle.racketeer.console.view.views.game

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.*
import dev.bitspittle.racketeer.console.view.View

class BrowseShopView(ctx: GameContext) : View(ctx) {
    override val subtitle = "Shop"

    override fun createCommands(): List<Command> = run {
        ctx.state.shop.stock.map { card ->
            if (card != null) BuyCardCommand(ctx, card) else SoldOutCardCommand(ctx)
        } + listOf(
            RestockShopCommand(ctx),
            if (ctx.state.shop.tier < ctx.data.maxTier) ExpandShopCommand(ctx) else MaxedOutShopCommand(ctx)
        )
    }
}