package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.views.BrowseDeckView
import dev.bitspittle.racketeer.console.view.views.BrowseDiscardView

class BrowseDiscardCommand(ctx: GameContext) : Command(ctx) {
    override val type = if (ctx.state.discard.cards.isNotEmpty()) Type.Read else Type.Disabled
    override val title = "Browse discard (${ctx.state.discard.cards.size})"

    override val description = "Look over the cards in the discard pile."

    override suspend fun invoke(): Boolean {
        ctx.viewStack.pushView(BrowseDiscardView(ctx))
        return true
    }
}

