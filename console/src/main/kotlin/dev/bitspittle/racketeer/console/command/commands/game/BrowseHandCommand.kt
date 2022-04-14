package dev.bitspittle.racketeer.console.command.commands.game

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.views.game.BrowseHandView

class BrowseHandCommand(ctx: GameContext) : Command(ctx) {
    override val type = if (ctx.state.hand.cards.isNotEmpty()) Type.Read else Type.Disabled
    override val title = "Browse hand (${ctx.state.hand.cards.size})"

    override val description = "Look over the cards in your hand."

    override suspend fun invoke(): Boolean {
        ctx.viewStack.pushView(BrowseHandView(ctx))
        return true
    }
}

