package dev.bitspittle.racketeer.console.view.views

import dev.bitspittle.racketeer.console.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.ViewCardCommand
import dev.bitspittle.racketeer.console.view.View
import dev.bitspittle.racketeer.model.game.GameConfig
import dev.bitspittle.racketeer.model.game.GameState

class BrowseDiscardView(ctx: GameContext) : View(ctx) {
    override val commands: List<Command> =
        ctx.state.discard.map { card -> ViewCardCommand(ctx, card) }
}