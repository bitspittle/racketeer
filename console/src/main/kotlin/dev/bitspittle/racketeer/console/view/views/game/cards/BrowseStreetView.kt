package dev.bitspittle.racketeer.console.view.views.game.cards

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.cards.ViewCardCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View

class BrowseStreetView(ctx: GameContext) : View(ctx) {
    init {
        check(ctx.state.street.cards.isNotEmpty())
    }

    override val subtitle = "The Street"

    override fun createCommands(): List<Command> =
        ctx.state.street.cards.map { card -> ViewCardCommand(ctx, card) }
}