package dev.bitspittle.racketeer.console.view.views.game.cards

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.cards.ViewCardGroupCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View

class BrowseJailView(ctx: GameContext) : View(ctx) {
    init {
        check(ctx.state.jail.cards.isNotEmpty())
    }

    override val subtitle = "Jail"

    override fun createCommands(): List<Command> =
        ctx.state.jail.cards
            .groupBy { it.template.name }
            .toSortedMap()
            .map { entry ->
                val group = entry.value
                ViewCardGroupCommand(ctx, group)
            }
}