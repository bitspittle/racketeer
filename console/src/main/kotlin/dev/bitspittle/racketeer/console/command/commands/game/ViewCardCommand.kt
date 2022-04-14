package dev.bitspittle.racketeer.console.command.commands.game

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.model.card.Card

/**
 * A no-op command used when read-only viewing a card
 */
class ViewCardCommand(ctx: GameContext, card: Card) : Command(ctx) {
    override val title = ctx.describer.describe(card, concise = true)
    override val description = ctx.describer.describe(card)
}
