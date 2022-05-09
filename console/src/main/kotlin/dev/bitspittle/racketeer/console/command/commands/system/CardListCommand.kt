package dev.bitspittle.racketeer.console.command.commands.system

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.views.game.cards.CardListView

class CardListCommand(ctx: GameContext) : Command(ctx) {
    override val title = "Card list"
    override val description: String = "Look through all the cards in the game and see what you've used (${ctx.data.cards.count { ctx.cardStats.contains(it.name) } * 100 / ctx.data.cards.size}%)."
    override suspend fun invoke(): Boolean {
        ctx.viewStack.pushView(CardListView(ctx))
        return true
    }
}
