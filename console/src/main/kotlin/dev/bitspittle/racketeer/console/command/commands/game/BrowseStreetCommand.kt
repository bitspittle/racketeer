package dev.bitspittle.racketeer.console.command.commands.game

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.views.game.BrowseStreetView

class BrowseStreetCommand(ctx: GameContext) : Command(ctx) {
    override val type = if (ctx.state.street.cards.isNotEmpty()) Type.Normal else Type.Disabled
    override val title = "Browse the street (${ctx.state.street.cards.size})"

    override val description = "Look over the cards in the street."

    override suspend fun invoke(): Boolean {
        ctx.viewStack.pushView(BrowseStreetView(ctx))
        return true
    }
}

