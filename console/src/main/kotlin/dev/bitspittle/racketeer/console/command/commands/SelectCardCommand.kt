package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.model.card.Card

class SelectCardCommand(
    ctx: GameContext, private val card: Card, var selected: Boolean = false
) : Command(ctx) {
    override val title get() = "[${if (selected) 'x' else ' '}] ${ctx.describer.describe(card, concise = true)}"
    override val description = ctx.describer.describe(card)

    override suspend fun invoke(): Boolean {
        selected = !selected
        return true
    }
}
