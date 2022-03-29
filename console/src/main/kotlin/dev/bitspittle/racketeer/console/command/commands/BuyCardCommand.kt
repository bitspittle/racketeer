package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.model.card.CardTemplate

class BuyCardCommand(ctx: GameContext, card: CardTemplate) : Command(ctx) {
    override val title = "Buy ${ctx.describers.describe(card)}"

    override val description = ctx.describers.describe(card, includeFlavor = true)
}

