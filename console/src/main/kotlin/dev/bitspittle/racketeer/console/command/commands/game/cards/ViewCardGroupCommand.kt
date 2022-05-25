package dev.bitspittle.racketeer.console.command.commands.game.cards

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.views.game.cards.BrowseCardsView
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.vpTotal

/**
 * A no-op command used when read-only viewing a group of card
 */
class ViewCardGroupCommand(ctx: GameContext, private val cards: List<Card>) : Command(ctx) {
    init {
        require(cards.isNotEmpty())
    }

    override val title = ctx.describer.describeCardGroupTitle(cards)
    override val description = ctx.describer.describeCardGroupBody(cards)
    override val extra = cards.sumOf { it.vpTotal }.takeIf { it > 0 }?.let { vp -> ctx.describer.describeVictoryPoints(vp) }

    override suspend fun invoke(): Boolean {
        // Sort the cards before viewing so we don't give away card order in this pile
        ctx.viewStack.pushView(BrowseCardsView(ctx, cards.sorted()))
        return true
    }
}
