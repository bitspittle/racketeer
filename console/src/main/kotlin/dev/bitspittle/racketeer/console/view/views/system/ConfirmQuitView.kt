package dev.bitspittle.racketeer.console.view.views.system

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.View

class ConfirmQuitView(ctx: GameContext) : View(ctx) {
    override fun createCommands(): List<Command> = listOf(
        object : Command(ctx) {
            override val type = Type.Danger
            override val title = "Confirm"

            override val description = "Press ENTER if you're sure you want to quit. Otherwise, go back!"

            override suspend fun invoke(): Boolean {
                ctx.app.quit()
                return true
            }
        }
    )

    // Don't allow people to enter this quit screen from within this quit screen
    override val allowQuit: Boolean = false
}