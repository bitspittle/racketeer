package dev.bitspittle.racketeer.console.command.commands.game

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.views.game.BrowseDeckView

class BrowseDeckCommand(ctx: GameContext) : Command(ctx) {
    override val type = if (ctx.state.deck.cards.isNotEmpty()) Type.Read else Type.Disabled
    override val title = "Browse deck (${ctx.state.deck.cards.size})"

    override val description = "Look over the cards in your deck."

    override suspend fun invoke(): Boolean {
        ctx.viewStack.pushView(BrowseDeckView(ctx))
        return true
    }
}

