package dev.bitspittle.racketeer.console.command.commands.game

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.model.card.Card

class SelectItemCommand(
    ctx: GameContext,
    val item: Any,
    var selected: Boolean = false,
    description: String? = null,
    override val type: Type = Type.Normal
) : Command(ctx) {
    override val title get() = "[${if (selected) 'x' else ' '}] ${describeForTitle(item)}"
    override val extra get() = describeForExtra(item)
    override val description = description ?: describeForDescription(item)

    override suspend fun invoke(): Boolean {
        selected = !selected
        return true
    }
}
