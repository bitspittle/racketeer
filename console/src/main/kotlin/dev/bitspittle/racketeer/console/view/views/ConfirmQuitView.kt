package dev.bitspittle.racketeer.console.view.views

import dev.bitspittle.racketeer.console.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.ConfirmQuitCommand
import dev.bitspittle.racketeer.console.command.commands.EmptyPileCommand
import dev.bitspittle.racketeer.console.command.commands.ViewCardCommand
import dev.bitspittle.racketeer.console.view.View
import dev.bitspittle.racketeer.model.game.GameConfig
import dev.bitspittle.racketeer.model.game.GameState

class ConfirmQuitView(ctx: GameContext) : View(ctx) {
    override val commands: List<Command> = listOf(ConfirmQuitCommand(ctx))

    // Don't allow people to enter this quit screen from within this quit screen
    override val allowQuit: Boolean = false
}