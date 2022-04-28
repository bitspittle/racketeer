package dev.bitspittle.racketeer.console.view.views.system

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.system.SaveGameCommand
import dev.bitspittle.racketeer.console.command.commands.system.SerializationSupport
import dev.bitspittle.racketeer.console.view.views.game.GameView

class ConfirmQuitView(ctx: GameContext) : GameView(ctx) {
    override fun createCommands(): List<Command> = listOf(
        object : Command(ctx) {
            override val type = Type.Warning
            override val title = "Confirm"

            override val description = "Press ENTER if you're sure you want to quit. This will create a quick save you can restore later. To keep playing, go back!"

            override suspend fun invoke(): Boolean {
                SaveGameCommand(ctx, SerializationSupport.QUICKSAVE_SLOT).invoke()
                ctx.app.quit()
                return true
            }
        }
    )
}