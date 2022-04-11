package dev.bitspittle.racketeer.console.view.views

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.BuyCardCommand
import dev.bitspittle.racketeer.console.command.commands.ExpandShopCommand
import dev.bitspittle.racketeer.console.command.commands.RestockShopCommand
import dev.bitspittle.racketeer.console.command.commands.SoldOutCardCommand
import dev.bitspittle.racketeer.console.view.View

class BrowseShopView(ctx: GameContext) : View(ctx) {
    override val subtitle = "Shop"

    override fun createCommands(): List<Command> = ctx.state.shop.stock.map { card ->
        if (card != null) BuyCardCommand(ctx, card) else SoldOutCardCommand(ctx)
    } + listOf(RestockShopCommand(ctx)) + if (ctx.state.shop.tier < ctx.data.maxTier) listOf(ExpandShopCommand(ctx)) else listOf()
}