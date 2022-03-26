package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.model.card.Card

class PlayCardCommand(ctx: GameContext, card: Card) : Command {
    override val title = "Play ${ctx.describers.describe(card)}"

    override val description = ctx.describers.describe(card, includeFlavor = true)
}

