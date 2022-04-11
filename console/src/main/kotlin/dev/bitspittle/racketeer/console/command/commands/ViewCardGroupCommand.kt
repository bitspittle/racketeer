package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.views.BrowseCardsView
import dev.bitspittle.racketeer.model.card.Card

/**
 * A no-op command used when read-only viewing a group of card
 */
class ViewCardGroupCommand(ctx: GameContext, private val cards: List<Card>) : Command(ctx) {
    init {
        require(cards.isNotEmpty())
    }

    private val representativeCard = cards.first()
    private val count = cards.size

    override val title = ctx.describer.describe(representativeCard, count, concise = true)
    override val description = ctx.describer.describe(representativeCard)

    override suspend fun invoke(): Boolean {
        ctx.viewStack.pushView(BrowseCardsView(ctx, cards))
        return true
    }
}
