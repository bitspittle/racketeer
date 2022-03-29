package dev.bitspittle.racketeer.console.view.views

import dev.bitspittle.racketeer.console.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.EmptyPileCommand
import dev.bitspittle.racketeer.console.command.commands.ViewCardCommand
import dev.bitspittle.racketeer.console.command.commands.ViewCardGroupCommand
import dev.bitspittle.racketeer.console.view.View
import dev.bitspittle.racketeer.model.game.GameConfig
import dev.bitspittle.racketeer.model.game.GameState

class BrowseDeckView(ctx: GameContext) : View(ctx) {
    override val commands: List<Command> =
        if (ctx.state.deck.cards.isNotEmpty()) {
            ctx.state.deck.cards
                .groupBy { it.template.name }
                .toSortedMap()
                .map { entry ->
                    val group = entry.value
                    ViewCardGroupCommand(ctx, group.first(), group.size)
                }
        }
        else {
            listOf(EmptyPileCommand(ctx))
        }
}