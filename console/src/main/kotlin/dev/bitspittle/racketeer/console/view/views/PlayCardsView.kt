package dev.bitspittle.racketeer.console.view.views

import dev.bitspittle.racketeer.console.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.BrowseDeckCommand
import dev.bitspittle.racketeer.console.command.commands.BrowseShopCommand
import dev.bitspittle.racketeer.console.command.commands.PlayCardCommand
import dev.bitspittle.racketeer.console.view.View

class PlayCardsView(ctx: GameContext) : View(ctx) {
    override val commands: List<Command> =
        ctx.state.hand.cards.map { card -> PlayCardCommand(ctx, card) } + listOf(BrowseShopCommand(ctx), BrowseDeckCommand(ctx))
}