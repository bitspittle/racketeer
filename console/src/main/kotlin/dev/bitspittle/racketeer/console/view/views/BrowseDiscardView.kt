package dev.bitspittle.racketeer.console.view.views

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.ViewCardCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View

class BrowseDiscardView(ctx: GameContext) : View(ctx) {
    init {
        check(ctx.state.discard.cards.isNotEmpty())
    }

    override val subtitle = "Discard"

    override val commands: List<Command> =
        ctx.state.discard.cards.map { card -> ViewCardCommand(ctx, card) }
}