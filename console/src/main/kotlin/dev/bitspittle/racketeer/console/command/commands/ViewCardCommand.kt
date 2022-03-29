package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate

/**
 * A no-op command used when read-only viewing a card
 */
class ViewCardCommand(ctx: GameContext, card: Card) : Command(ctx) {
    override val title = ctx.describers.describe(card)
    override val description = ctx.describers.describe(card, includeFlavor = true)
}
