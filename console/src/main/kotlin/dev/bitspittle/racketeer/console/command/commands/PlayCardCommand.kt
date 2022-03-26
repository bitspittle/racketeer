package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.model.card.Card

class PlayCardCommand(ctx: GameContext, card: Card) : Command {
    override val title = card.toString()

    override val description = ctx.describers.describe(card)
}

