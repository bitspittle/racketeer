package dev.bitspittle.racketeer.console.view.views

import dev.bitspittle.racketeer.console.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.BuyCardCommand
import dev.bitspittle.racketeer.console.command.commands.ExpandShopCommand
import dev.bitspittle.racketeer.console.view.View

class BrowseShopView(ctx: GameContext) : View(ctx) {
    override val commands: List<Command> =
        ctx.state.shop.cards.map { card -> BuyCardCommand(ctx, card) } + listOf(ExpandShopCommand(ctx))
}