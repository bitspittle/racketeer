package dev.bitspittle.racketeer.console.view.views.game.cards

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.cards.ViewCardGroupCommand
import dev.bitspittle.racketeer.console.view.views.game.GameView

class BrowseDeckView(ctx: GameContext) : GameView(ctx) {
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