package dev.bitspittle.racketeer.console.command.commands.game.cards

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.views.game.cards.BrowseManyCardsView
import dev.bitspittle.racketeer.model.game.getOwnedCards

class BrowseOwnedCardsCommand(ctx: GameContext) : Command(ctx) {
    private val owned = ctx.state.getOwnedCards().toList()
    override val type = if (owned.isNotEmpty()) Type.Normal else Type.Disabled
    override val title = "Browse all owned (${owned.size})"

    override val description = "See all your owned cards (which are all piles excluding the jail)."

    override suspend fun invoke(): Boolean {
        ctx.viewStack.pushView(BrowseManyCardsView(ctx, owned))
        return true
    }
}

