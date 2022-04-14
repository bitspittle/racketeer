package dev.bitspittle.racketeer.console.view.views.game

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.ViewCardGroupCommand
import dev.bitspittle.racketeer.console.view.View

class BrowseDeckView(ctx: GameContext) : View(ctx) {
    init {
        check(ctx.state.deck.cards.isNotEmpty())
    }

    override val subtitle = "Deck"

    override fun createCommands(): List<Command> =
        ctx.state.deck.cards
            .groupBy { it.template.name }
            .toSortedMap()
            .map { entry ->
                val group = entry.value
                ViewCardGroupCommand(ctx, group)
            }
}