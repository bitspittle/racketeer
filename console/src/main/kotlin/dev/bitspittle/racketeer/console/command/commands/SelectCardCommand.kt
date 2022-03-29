package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate

class SelectCardCommand(
    ctx: GameContext, private val card: Card, var selected: Boolean = false
) : Command(ctx) {
    override val title get() = "[${if (selected) 'x' else ' '}] ${ctx.describers.describe(card)}"
    override val description = ctx.describers.describe(card, includeFlavor = true)

    override fun invoke() {
        selected = !selected
    }
}
