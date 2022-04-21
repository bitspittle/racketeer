package dev.bitspittle.racketeer.console.command.commands.game

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.views.game.BrowseManyCardsView

class BrowseOwnedCardsCommand(ctx: GameContext) : Command(ctx) {
    private val owned = ctx.state.getOwnedCards()
    override val type = if (owned.isNotEmpty()) Type.Read else Type.Disabled
    override val title = "Browse all owned (${owned.size})"

    override val description = "See all your owned cards (which are all piles excluding the jail)."

    override suspend fun invoke(): Boolean {
        ctx.viewStack.pushView(BrowseManyCardsView(ctx, owned))
        return true
    }
}

