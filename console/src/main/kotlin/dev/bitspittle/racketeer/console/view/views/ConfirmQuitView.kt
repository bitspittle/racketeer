package dev.bitspittle.racketeer.console.view.views

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.View

class ConfirmQuitView(ctx: GameContext) : View(ctx) {
    override val commands: List<Command> = listOf(
        object : Command(ctx) {
            override val title = "Confirm"

            override val description = "Press ENTER if you're sure you want to quit. Otherwise, go back!"

            override fun invoke() {
                ctx.app.quit()
            }
        }
    )

    // Don't allow people to enter this quit screen from within this quit screen
    override val allowQuit: Boolean = false
}