package dev.bitspittle.racketeer.console.view.views.system

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.system.NewGameCommand
import dev.bitspittle.racketeer.console.view.views.game.GameView

class ConfirmRestartView(ctx: GameContext) : GameView(ctx) {
    override fun createCommands(): List<Command> = listOf(
        object : Command(ctx) {
            override val type = Type.Danger
            override val title = "Confirm"

            override val description = "Press ENTER if you're sure you want to restart this game. Otherwise, go back!"

            override suspend fun invoke(): Boolean {
                return NewGameCommand(ctx).invoke()
            }
        }
    )
}