package dev.bitspittle.racketeer.console.view.views

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.ViewCardGroupCommand
import dev.bitspittle.racketeer.console.view.View

class BrowseDeckView(ctx: GameContext) : View(ctx) {
    init {
        check(ctx.state.deck.cards.isNotEmpty())
    }

    override val subtitle = "Deck"

    override val commands: List<Command> =
        ctx.state.deck.cards
            .groupBy { it.template.name }
            .toSortedMap()
            .map { entry ->
                val group = entry.value
                ViewCardGroupCommand(ctx, group.first(), group.size)
            }
}