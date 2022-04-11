package dev.bitspittle.racketeer.console.view.views

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.ViewCardCommand
import dev.bitspittle.racketeer.console.command.commands.ViewCardGroupCommand
import dev.bitspittle.racketeer.console.view.View

class BrowseHandView(ctx: GameContext) : View(ctx) {
    init {
        check(ctx.state.hand.cards.isNotEmpty())
    }

    override val subtitle = "Hand"

    override fun createCommands(): List<Command> =
        ctx.state.hand.cards.map { card -> ViewCardCommand(ctx, card) }
}