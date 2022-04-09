package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.model.card.Card

class SelectItemCommand(
    ctx: GameContext, val item: Any, var selected: Boolean = false
) : Command(ctx) {
    override val title get() = "[${if (selected) 'x' else ' '}] ${describeTitle(item)}"
    override val description = describeDescription(item)

    private fun describeTitle(item: Any): String {
        return when (item) {
            is Card -> ctx.describer.describe(item, concise = true)
            else -> item.toString()
        }
    }

    private fun describeDescription(item: Any): String? {
        return when (item) {
            is Card -> ctx.describer.describe(item, concise = false )
            else -> null
        }
    }

    override suspend fun invoke(): Boolean {
        selected = !selected
        return true
    }
}
