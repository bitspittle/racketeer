package dev.bitspittle.racketeer.console.command.commands.system

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.views.game.BrowseAllCardsView

class BrowseAllCardsCommand(ctx: GameContext) : Command(ctx) {
    override val title = "Browse all cards"
    override val description: String = "Look through all the cards in the game and see what you've used."
    override suspend fun invoke(): Boolean {
        ctx.viewStack.pushView(BrowseAllCardsView(ctx))
        return true
    }
}
