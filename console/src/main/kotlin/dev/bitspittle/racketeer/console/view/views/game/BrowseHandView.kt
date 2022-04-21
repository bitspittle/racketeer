package dev.bitspittle.racketeer.console.view.views.game

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.ViewCardCommand

class BrowseHandView(ctx: GameContext) : GameView(ctx) {
    init {
        check(ctx.state.hand.cards.isNotEmpty())
    }

    override val subtitle = "Hand"

    override fun createCommands(): List<Command> =
        ctx.state.hand.cards.map { card -> ViewCardCommand(ctx, card) }
}