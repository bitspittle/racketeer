package dev.bitspittle.racketeer.console.view.views.system

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.system.LoadGameCommand
import dev.bitspittle.racketeer.console.command.commands.system.SaveGameCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.views.game.GameView

class LoadGameView(ctx: GameContext) : GameView(ctx) {
    override fun createCommands(): List<Command> = (0 until 10).map { slot ->
        LoadGameCommand(ctx, slot)
    }
}