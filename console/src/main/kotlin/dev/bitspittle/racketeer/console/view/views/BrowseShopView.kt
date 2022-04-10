package dev.bitspittle.racketeer.console.view.views

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.BuyCardCommand
import dev.bitspittle.racketeer.console.command.commands.ExpandShopCommand
import dev.bitspittle.racketeer.console.command.commands.SoldOutCardCommand
import dev.bitspittle.racketeer.console.view.View

class BrowseShopView(ctx: GameContext) : View(ctx) {
    override val subtitle = "Shop"

    override val commands: List<Command> =
        ctx.state.shop.stock.map { card ->
        if (card != null) BuyCardCommand(ctx, card) else SoldOutCardCommand(ctx) } + listOf(ExpandShopCommand(ctx))
}