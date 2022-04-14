package dev.bitspittle.racketeer.console.command.commands.game

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.views.game.BrowseCardsView
import dev.bitspittle.racketeer.model.card.Card

/**
 * A no-op command used when read-only viewing a group of card
 */
class ViewCardGroupCommand(ctx: GameContext, private val cards: List<Card>) : Command(ctx) {
    init {
        require(cards.isNotEmpty())
    }

    override val title = ctx.describer.describe(cards, concise = true)
    override val description = ctx.describer.describe(cards)

    override suspend fun invoke(): Boolean {
        // Sort the cards before viewing so we don't give away card order in this pile
        ctx.viewStack.pushView(BrowseCardsView(ctx, cards.sorted()))
        return true
    }
}
