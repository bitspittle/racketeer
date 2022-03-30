package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.model.card.Card

/**
 * A no-op command used when read-only viewing a group of card
 */
class ViewCardGroupCommand(ctx: GameContext, card: Card, count: Int) : Command(ctx) {
    override val title = ctx.describers.describe(card, count, concise = true)
    override val description = ctx.describers.describe(card)
}