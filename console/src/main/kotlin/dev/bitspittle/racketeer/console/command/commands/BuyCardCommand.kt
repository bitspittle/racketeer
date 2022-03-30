package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.model.card.CardTemplate

class BuyCardCommand(ctx: GameContext, card: CardTemplate) : Command(ctx) {
    override val title = "Buy: ${ctx.describers.describe(card, concise = true)}"

    override val description = ctx.describers.describe(card)
}

