package dev.bitspittle.racketeer.console.view.views.game

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.ViewCardCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View

class BrowseDiscardView(ctx: GameContext) : View(ctx) {
    init {
        check(ctx.state.discard.cards.isNotEmpty())
    }

    override val subtitle = "Discard"

    override fun createCommands(): List<Command> =
        ctx.state.discard.cards.map { card -> ViewCardCommand(ctx, card) }
}