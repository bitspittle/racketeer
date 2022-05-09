package dev.bitspittle.racketeer.console.command.commands.game.cards

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.views.game.cards.BrowseDiscardView

class BrowseDiscardCommand(ctx: GameContext) : Command(ctx) {
    override val type = if (ctx.state.discard.cards.isNotEmpty()) Type.Normal else Type.Disabled
    override val title = "Browse discard (${ctx.state.discard.cards.size})"

    override val description = "Look over the cards in the discard pile."

    override suspend fun invoke(): Boolean {
        ctx.viewStack.pushView(BrowseDiscardView(ctx))
        return true
    }
}

