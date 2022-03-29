package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.model.card.Card

class PlayCardCommand(ctx: GameContext, card: Card) : Command(ctx) {
    override val title = "Play ${ctx.describers.describe(card, concise = true)}"

    override val description = ctx.describers.describe(card)
}

