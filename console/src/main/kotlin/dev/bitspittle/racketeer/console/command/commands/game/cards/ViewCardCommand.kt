package dev.bitspittle.racketeer.console.command.commands.game.cards

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.model.card.Card

/**
 * A no-op command used when read-only viewing a card
 */
class ViewCardCommand(ctx: GameContext, card: Card, override val extra: String? = null) : Command(ctx) {
    override val title = ctx.describer.describeCardTitle(card)
    override val description = ctx.describer.describeCardBody(card)
}
