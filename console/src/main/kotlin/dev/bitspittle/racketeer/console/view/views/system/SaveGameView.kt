package dev.bitspittle.racketeer.console.view.views.system

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.system.SaveGameCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.views.game.GameView

class SaveGameView(ctx: GameContext) : GameView(ctx) {
    override fun createCommands(): List<Command> = (0 until 10).map { slot ->
        SaveGameCommand(ctx, slot)
    }
}