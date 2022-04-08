package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.views.BrowseDeckView

class BrowseDeckCommand(ctx: GameContext) : Command(ctx) {
    override val title = "Browse deck (${ctx.state.deck.cards.size})"

    override val description = "Look over the cards in your deck."

    override suspend fun invoke(): Boolean {
        return if (ctx.state.deck.cards.isNotEmpty()) {
            ctx.viewStack.pushView(BrowseDeckView(ctx))
            true
        } else {
            false
        }
    }
}

